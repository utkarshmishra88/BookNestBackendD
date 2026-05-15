package com.booknest.order.service.impl;

import com.booknest.order.client.CartClient;
import com.booknest.order.client.PaymentClient;
import com.booknest.order.client.WalletServiceClient;
import com.booknest.order.dto.OrderItemResponse;
import com.booknest.order.dto.OrderResponse;
import com.booknest.order.dto.PlaceOrderRequest;
import com.booknest.order.dto.VerifyPaymentRequest;
import com.booknest.order.entity.Order;
import com.booknest.order.entity.OrderItem;
import com.booknest.order.enums.OrderStatus;
import com.booknest.order.enums.PaymentMode;
import com.booknest.order.repository.OrderRepository;
import com.booknest.order.service.OrderConfirmationNotifier;
import com.booknest.order.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static final ObjectMapper JSON = new ObjectMapper();

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final PaymentClient paymentClient;
    private final WalletServiceClient walletServiceClient;
    private final OrderConfirmationNotifier orderConfirmationNotifier;
    private final com.booknest.order.client.BookClient bookClient;
    private final com.booknest.order.client.AuthInternalClient authInternalClient;
    private final com.booknest.order.service.OrderPdfDocumentService pdfDocumentService;
    private final com.booknest.order.repository.CouponRepository couponRepository;

    @org.springframework.beans.factory.annotation.Value("${booknest.internal-api-key:}")
    private String internalApiKey;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartClient cartClient,
                            PaymentClient paymentClient,
                            WalletServiceClient walletServiceClient,
                            OrderConfirmationNotifier orderConfirmationNotifier,
                            com.booknest.order.client.BookClient bookClient,
                            com.booknest.order.client.AuthInternalClient authInternalClient,
                            com.booknest.order.service.OrderPdfDocumentService pdfDocumentService,
                            com.booknest.order.repository.CouponRepository couponRepository) {
        this.orderRepository = orderRepository;
        this.cartClient = cartClient;
        this.paymentClient = paymentClient;
        this.walletServiceClient = walletServiceClient;
        this.orderConfirmationNotifier = orderConfirmationNotifier;
        this.bookClient = bookClient;
        this.authInternalClient = authInternalClient;
        this.pdfDocumentService = pdfDocumentService;
        this.couponRepository = couponRepository;
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "catalogService", fallbackMethod = "catalogFallback")
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        // 1. Fetch cart
        Map<String, Object> cart = cartClient.getCartByUserId(userId);
        List<Map<String, Object>> cartItems = extractCartItems(cart);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 2. Build order
        Order order = new Order();
        order.setUserId(userId);
        order.setPaymentMode(request.getPaymentMode());
        order.setShippingAddressId(request.getShippingAddressId());
        order.setStatus(
                request.getPaymentMode() == PaymentMode.COD
                        ? OrderStatus.CONFIRMED
                        : OrderStatus.PENDING_PAYMENT
        );

        // 3. Build order items
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> ci : cartItems) {
            BigDecimal price    = asBigDecimal(getAny(ci, "price", "bookPrice", "unitPrice"), BigDecimal.ZERO);
            Integer    qty      = asInteger(getAny(ci, "quantity", "qty"), 1);
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

            Object bookObj = ci.get("book");
            Map<String, Object> bookMap = bookObj instanceof Map ? (Map<String, Object>) bookObj : null;
            
            String title = asString(getAny(ci, "title", "bookTitle", "name"), null);
            String author = asString(getAny(ci, "author", "bookAuthor"), null);
            String imageUrl = asString(getAny(ci, "imageUrl", "coverImage", "bookImage"), null);
            
            if (bookMap != null) {
                if (title == null) title = asString(getAny(bookMap, "title", "name"), "Unknown");
                if (author == null) author = asString(getAny(bookMap, "author"), null);
                if (imageUrl == null) imageUrl = asString(getAny(bookMap, "imageUrl", "coverImage"), null);
            }
            Long bookIdValue = asLong(getAny(ci, "bookId", "id"), 0L);
            if (title == null || title.equals("Unknown")) {
                try {
                    Map<String, Object> remoteBook = bookClient.getBookById(bookIdValue.intValue());
                    if (remoteBook != null) {
                        title = asString(getAny(remoteBook, "title", "name"), "Unknown");
                        if (author == null) author = asString(getAny(remoteBook, "author"), null);
                        if (imageUrl == null) imageUrl = asString(getAny(remoteBook, "imageUrl", "coverImage"), null);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to fetch book details for order item: " + e.getMessage());
                }
            }
            if (title == null) title = "Unknown";

            OrderItem item = OrderItem.builder()
                    .bookId(bookIdValue)
                    .title(title)
                    .author(author)
                    .imageUrl(imageUrl)
                    .price(price)
                    .priceAtPurchase(price)   // snapshot of price at time of purchase
                    .quantity(qty)
                    .lineTotal(lineTotal)
                    .build();

            order.addItem(item);
            total = total.add(lineTotal);
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid total amount: " + total);
        }
        
        // 3.5 Apply Coupon if present
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            com.booknest.order.entity.Coupon coupon = ((com.booknest.order.repository.CouponRepository) this.couponRepository).findByCode(request.getCouponCode())
                    .orElseThrow(() -> new RuntimeException("Invalid coupon code"));
            
            if (!coupon.isActive()) throw new RuntimeException("Coupon is inactive");
            if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(java.time.LocalDate.now())) 
                throw new RuntimeException("Coupon has expired");
            if (total.compareTo(coupon.getMinOrderAmount()) < 0)
                throw new RuntimeException("Minimum order amount for this coupon is ₹" + coupon.getMinOrderAmount());
            
            if (coupon.getDiscountPercentage() != null && coupon.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                discount = total.multiply(coupon.getDiscountPercentage()).divide(new BigDecimal(100));
                
                // Apply MAX CAP
                if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                    if (discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                        discount = coupon.getMaxDiscountAmount();
                    }
                }
            } else {
                discount = coupon.getDiscountAmount();
            }
            
            order.setCouponCode(coupon.getCode());
            order.setDiscountAmount(discount);
        }
        
        BigDecimal finalTotal = total.subtract(discount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;
        
        order.setTotalAmount(finalTotal);

        // 4. Persist order (with items via cascade)
        order = orderRepository.save(order);

        // 5. Handle payment mode
        String razorpayOrderId = null;
        String razorpayKeyId   = null;
        String message         = "Order created";

        switch (request.getPaymentMode()) {
            case COD -> {
                safeClearCart(userId);
                message = "COD order placed successfully";
            }
            case WALLET -> {
                try {
                    Map<String, Object> walletReq = new HashMap<>();
                    walletReq.put("amount", total);
                    walletReq.put("orderId", order.getOrderId());
                    walletReq.put("remarks", "Order #" + order.getOrderId() + " payment");

                    Map<String, Object> walletResp = walletServiceClient.debit(userId, walletReq);
                    String status = asString(getAny(walletResp, "status"), "");

                    if ("SUCCESS".equalsIgnoreCase(status)) {
                        order.setStatus(OrderStatus.CONFIRMED);
                        safeClearCart(userId);
                        message = "Wallet payment processed successfully";
                    } else {
                        order.setStatus(OrderStatus.FAILED);
                        message = asString(getAny(walletResp, "message"), "Wallet payment failed");
                    }
                } catch (Exception e) {
                    order.setStatus(OrderStatus.FAILED);
                    message = "Wallet debit failed: " + e.getMessage();
                }
                order = orderRepository.save(order);
            }
            default -> {
                // CARD, UPI, NETBANKING — create Razorpay order
                Map<String, Object> paymentReq = new HashMap<>();
                paymentReq.put("orderId", order.getOrderId());
                paymentReq.put("userId", userId);
                paymentReq.put("amount", total);
                paymentReq.put("paymentMode", request.getPaymentMode().name());

                Map<String, Object> paymentResp;
                try {
                    paymentResp = paymentClient.createPaymentOrder(paymentReq);
                } catch (FeignException ex) {
                    String detail = feignErrorMessage(ex.contentUTF8());
                    throw new RuntimeException(
                            detail != null && !detail.isBlank()
                                    ? detail
                                    : "Unable to start online payment. Please try again in a few seconds.");
                }

                Long   paymentId    = asLong(getAny(paymentResp, "paymentId"), null);
                razorpayOrderId     = asString(getAny(paymentResp, "razorpayOrderId"), null);
                razorpayKeyId       = asString(getAny(paymentResp, "razorpayKeyId"), null);
                message             = asString(getAny(paymentResp, "message"), "Payment order created");

                if (paymentId != null)      order.setPaymentId(paymentId);
                if (razorpayOrderId != null) order.setPaymentGatewayOrderId(razorpayOrderId);

                String paymentStatus = asString(getAny(paymentResp, "status"), "");
                if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    safeClearCart(userId);
                }
                order = orderRepository.save(order);
            }
        }

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            orderConfirmationNotifier.notifyOrderConfirmed(order);
            reduceStockForOrder(order);
        }

        return toOrderResponse(order, razorpayOrderId, razorpayKeyId, message);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public OrderResponse verifyPayment(Long userId, VerifyPaymentRequest request) {
        Order order = orderRepository.findByOrderIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Map<String, Object> verifyReq = new HashMap<>();
        verifyReq.put("paymentId",           request.getPaymentId());
        verifyReq.put("razorpayOrderId",     request.getRazorpayOrderId());
        verifyReq.put("razorpayPaymentId",   request.getRazorpayPaymentId());
        verifyReq.put("razorpaySignature",   request.getRazorpaySignature());

        Map<String, Object> verifyResp = paymentClient.verifyPayment(verifyReq);
        String status  = asString(getAny(verifyResp, "status"), "");
        String message = asString(getAny(verifyResp, "message"), "Payment verification completed");

        if ("SUCCESS".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status)) {
            order.setStatus(OrderStatus.CONFIRMED);
            safeClearCart(userId);
        } else {
            order.setStatus(OrderStatus.FAILED);
        }

        order = orderRepository.save(order);
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            orderConfirmationNotifier.notifyOrderConfirmed(order);
            reduceStockForOrder(order);
        }
        return toOrderResponse(order, order.getPaymentGatewayOrderId(), null, message);
    }

    // ─── Fallback methods ──────────────────────────────────────────────────────

    public OrderResponse catalogFallback(Long userId, PlaceOrderRequest request, Throwable t) {
        log.error("Circuit Breaker [catalogService] triggered: {}", t.getMessage());
        return OrderResponse.builder()
                .message("We're experiencing issues fetching book details. Please try again in a few moments.")
                .build();
    }

    public OrderResponse paymentFallback(Long userId, VerifyPaymentRequest request, Throwable t) {
        log.error("Circuit Breaker [paymentService] triggered: {}", t.getMessage());
        return OrderResponse.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .status(OrderStatus.PENDING_PAYMENT)
                .message("Payment verification service is currently unavailable. Your order status will be updated automatically once the service is restored.")
                .build();
    }

    @Override
    @Transactional
    public List<OrderResponse> getAllOrdersForAdmin() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(o -> toOrderResponse(o, o.getPaymentGatewayOrderId(), null, "OK"))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(o -> toOrderResponse(o, o.getPaymentGatewayOrderId(), null, "OK"))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return toOrderResponse(order, order.getPaymentGatewayOrderId(), null, "OK");
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
                
        if (status == OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.CONFIRMED) {
            reduceStockForOrder(order);
            orderConfirmationNotifier.notifyOrderConfirmed(order);
        }
        
        order.setStatus(status);
        order = orderRepository.save(order);
        
        if (status != OrderStatus.CONFIRMED && status != OrderStatus.PENDING_PAYMENT) {
            orderConfirmationNotifier.notifyOrderStatusUpdated(order);
        }
        
        return toOrderResponse(order, order.getPaymentGatewayOrderId(), null, "Status updated to " + status);
    }

    @Override
    @Transactional
    public byte[] getInvoicePdf(Long userId, Long orderId) {
        Order order = orderRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        Map<String, Object> addressMap = null;
        if (order.getShippingAddressId() != null) {
            try {
                addressMap = authInternalClient.getAddress(internalApiKey, order.getShippingAddressId().intValue());
            } catch (Exception e) {
                log.warn("Could not fetch address for invoice PDF: {}", e.getMessage());
            }
        }
        
        try {
            return pdfDocumentService.buildInvoicePdf(order, addressMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    @Override
    @Transactional
    public byte[] getReceiptPdf(Long userId, Long orderId) {
        Order order = orderRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        try {
            return pdfDocumentService.buildPaymentReceiptPdf(order);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate receipt PDF", e);
        }
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    private void reduceStockForOrder(Order order) {
        try {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getBookId() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                        bookClient.updateStock(item.getBookId().intValue(), -item.getQuantity());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to reduce stock for order {}: {}", order.getOrderId(), e.getMessage());
        }
    }

    private void safeClearCart(Long userId) {
        try {
            cartClient.clearCart(userId);
        } catch (Exception ignored) { }
    }

    private List<Map<String, Object>> extractCartItems(Map<String, Object> cart) {
        Object itemsObj = getAny(cart, "items", "cartItems", "data");
        if (itemsObj instanceof List<?> rawList) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object obj : rawList) {
                if (obj instanceof Map<?, ?> m) {
                    Map<String, Object> entry = new HashMap<>();
                    for (Map.Entry<?, ?> e : m.entrySet()) {
                        entry.put(String.valueOf(e.getKey()), e.getValue());
                    }
                    out.add(entry);
                }
            }
            return out;
        }
        return Collections.emptyList();
    }

    private OrderResponse toOrderResponse(Order order, String razorpayOrderId,
                                          String razorpayKeyId, String message) {
        // Eagerly load items within transaction context
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .orderItemId(i.getOrderItemId())
                        .bookId(i.getBookId())
                        .title(i.getTitle())
                        .author(i.getAuthor())
                        .imageUrl(i.getImageUrl())
                        .price(i.getPrice())
                        .quantity(i.getQuantity())
                        .lineTotal(i.getLineTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .paymentMode(order.getPaymentMode())
                .status(order.getStatus())
                .paymentId(order.getPaymentId())
                .paymentGatewayOrderId(order.getPaymentGatewayOrderId())
                .razorpayOrderId(razorpayOrderId)
                .razorpayKeyId(razorpayKeyId)
                .message(message)
                .createdAt(order.getCreatedAt())
                .couponCode(order.getCouponCode())
                .discountAmount(order.getDiscountAmount())
                .items(itemResponses)
                .build();
    }

    private Object getAny(Map<String, Object> source, String... keys) {
        if (source == null) return null;
        for (String k : keys) {
            if (source.containsKey(k)) return source.get(k);
        }
        return null;
    }

    private String asString(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private Long asLong(Object value, Long fallback) {
        if (value == null) return fallback;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof Long l)    return l;
        if (value instanceof String s)  {
            try { return Long.parseLong(s); } catch (Exception ignored) { }
        }
        return fallback;
    }

    private Integer asInteger(Object value, Integer fallback) {
        if (value == null) return fallback;
        if (value instanceof Integer i) return i;
        if (value instanceof Long l)    return l.intValue();
        if (value instanceof String s)  {
            try { return Integer.parseInt(s); } catch (Exception ignored) { }
        }
        return fallback;
    }

    private BigDecimal asBigDecimal(Object value, BigDecimal fallback) {
        if (value == null) return fallback;
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String feignErrorMessage(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode n = JSON.readTree(body);
            if (n.hasNonNull("message")) {
                return n.get("message").asText();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}