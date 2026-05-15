package com.booknest.order.service.impl;

import com.booknest.order.client.CartClient;
import com.booknest.order.client.PaymentClient;
import com.booknest.order.client.WalletServiceClient;
import com.booknest.order.dto.OrderResponse;
import com.booknest.order.dto.PlaceOrderRequest;
import com.booknest.order.dto.VerifyPaymentRequest;
import com.booknest.order.entity.Order;
import com.booknest.order.entity.OrderItem;
import com.booknest.order.enums.OrderStatus;
import com.booknest.order.enums.PaymentMode;
import com.booknest.order.repository.OrderRepository;
import com.booknest.order.service.OrderConfirmationNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartClient cartClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private WalletServiceClient walletServiceClient;

    @Mock
    private OrderConfirmationNotifier orderConfirmationNotifier;

    @Mock
    private com.booknest.order.client.BookClient bookClient;

    @Mock
    private com.booknest.order.client.AuthInternalClient authInternalClient;

    @Mock
    private com.booknest.order.service.OrderPdfDocumentService pdfDocumentService;

    @Mock
    private com.booknest.order.repository.CouponRepository couponRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private PlaceOrderRequest placeOrderRequest;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderId(1L);
        order.setUserId(1L);
        order.setTotalAmount(BigDecimal.valueOf(100.0));
        order.setPaymentMode(PaymentMode.COD);
        order.setStatus(OrderStatus.CONFIRMED);

        OrderItem item = new OrderItem();
        item.setOrderItemId(1L);
        item.setBookId(10L);
        item.setPrice(BigDecimal.valueOf(100.0));
        item.setQuantity(1);
        item.setLineTotal(BigDecimal.valueOf(100.0));
        order.addItem(item);

        placeOrderRequest = new PlaceOrderRequest();
        placeOrderRequest.setPaymentMode(PaymentMode.COD);
        placeOrderRequest.setShippingAddressId(1L);
    }

    @Test
    void testPlaceOrder_WalletSuccess() {
        Map<String, Object> cartResponse = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("bookId", 10);
        item1.put("price", 100.0);
        item1.put("quantity", 1);
        items.add(item1);
        cartResponse.put("items", items);

        placeOrderRequest.setPaymentMode(PaymentMode.WALLET);
        
        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        Map<String, Object> walletResp = new HashMap<>();
        walletResp.put("status", "SUCCESS");
        when(walletServiceClient.debit(anyLong(), anyMap())).thenReturn(walletResp);

        OrderResponse response = orderService.placeOrder(1L, placeOrderRequest);

        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        verify(walletServiceClient).debit(anyLong(), anyMap());
    }

    @Test
    void testPlaceOrder_WalletFailure() {
        Map<String, Object> cartResponse = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(Map.of("bookId", 10, "price", 100.0, "quantity", 1));
        cartResponse.put("items", items);

        placeOrderRequest.setPaymentMode(PaymentMode.WALLET);
        
        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        Map<String, Object> walletResp = new HashMap<>();
        walletResp.put("status", "FAILURE");
        walletResp.put("message", "Insufficient Balance");
        when(walletServiceClient.debit(anyLong(), anyMap())).thenReturn(walletResp);

        OrderResponse response = orderService.placeOrder(1L, placeOrderRequest);

        assertEquals(OrderStatus.FAILED, response.getStatus());
        assertEquals("Insufficient Balance", response.getMessage());
    }

    @Test
    void testPlaceOrder_WithCouponSuccess() {
        Map<String, Object> cartResponse = new HashMap<>();
        cartResponse.put("items", List.of(Map.of("bookId", 10, "price", 1000.0, "quantity", 1)));

        placeOrderRequest.setCouponCode("SAVE10");
        
        com.booknest.order.entity.Coupon coupon = new com.booknest.order.entity.Coupon();
        coupon.setCode("SAVE10");
        coupon.setActive(true);
        coupon.setDiscountPercentage(new BigDecimal("10"));
        coupon.setMinOrderAmount(new BigDecimal("500"));
        coupon.setMaxDiscountAmount(new BigDecimal("200"));
        
        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.placeOrder(1L, placeOrderRequest);

        verify(orderRepository, atLeastOnce()).save(argThat(o -> 
            o.getDiscountAmount().compareTo(new BigDecimal("100.00")) == 0 &&
            o.getTotalAmount().compareTo(new BigDecimal("900.00")) == 0
        ));
    }

    @Test
    void testPlaceOrder_InvalidCoupon() {
        Map<String, Object> cartResponse = new HashMap<>();
        cartResponse.put("items", List.of(Map.of("bookId", 10, "price", 1000.0, "quantity", 1)));
        placeOrderRequest.setCouponCode("INVALID");

        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.placeOrder(1L, placeOrderRequest));
    }

    @Test
    void testPlaceOrder_ExpiredCoupon() {
        Map<String, Object> cartResponse = new HashMap<>();
        cartResponse.put("items", List.of(Map.of("bookId", 10, "price", 1000.0, "quantity", 1)));
        placeOrderRequest.setCouponCode("OLD");

        com.booknest.order.entity.Coupon coupon = new com.booknest.order.entity.Coupon();
        coupon.setCode("OLD");
        coupon.setActive(true);
        coupon.setExpiryDate(java.time.LocalDate.now().minusDays(1));

        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(couponRepository.findByCode("OLD")).thenReturn(Optional.of(coupon));

        assertThrows(RuntimeException.class, () -> orderService.placeOrder(1L, placeOrderRequest));
    }

    @Test
    void testCatalogFallback() {
        Throwable t = new RuntimeException("Service Down");
        OrderResponse response = orderService.catalogFallback(1L, placeOrderRequest, t);
        assertTrue(response.getMessage().contains("experiencing issues"));
    }

    @Test
    void testPaymentFallback() {
        VerifyPaymentRequest req = new VerifyPaymentRequest();
        req.setOrderId(1L);
        Throwable t = new RuntimeException("Service Down");
        OrderResponse response = orderService.paymentFallback(1L, req, t);
        assertEquals(OrderStatus.PENDING_PAYMENT, response.getStatus());
        assertTrue(response.getMessage().contains("currently unavailable"));
    }

    @Test
    void testGetInvoicePdf_Success() throws Exception {
        when(orderRepository.findByOrderIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));
        when(pdfDocumentService.buildInvoicePdf(any(), any())).thenReturn(new byte[]{1, 2, 3});

        byte[] pdf = orderService.getInvoicePdf(1L, 1L);
        assertArrayEquals(new byte[]{1, 2, 3}, pdf);
    }

    @Test
    void testGetReceiptPdf_Success() throws Exception {
        when(orderRepository.findByOrderIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));
        when(pdfDocumentService.buildPaymentReceiptPdf(any())).thenReturn(new byte[]{4, 5, 6});

        byte[] pdf = orderService.getReceiptPdf(1L, 1L);
        assertArrayEquals(new byte[]{4, 5, 6}, pdf);
    }

    @Test
    void testUpdateOrderStatus_ConfirmedTriggersStockReduction() {
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        verify(bookClient).updateStock(10, -1);
        verify(orderConfirmationNotifier).notifyOrderConfirmed(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_ShippedTriggersNotification() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.updateOrderStatus(1L, OrderStatus.SHIPPED);

        verify(orderConfirmationNotifier).notifyOrderStatusUpdated(any(Order.class));
    }

    @Test
    void testVerifyPayment_Success() {
        VerifyPaymentRequest req = new VerifyPaymentRequest();
        req.setOrderId(1L);
        req.setPaymentId(123L);

        when(orderRepository.findByOrderIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentClient.verifyPayment(anyMap())).thenReturn(Map.of("status", "SUCCESS", "message", "Verified"));

        OrderResponse resp = orderService.verifyPayment(1L, req);

        assertEquals(OrderStatus.CONFIRMED, resp.getStatus());
        verify(orderConfirmationNotifier).notifyOrderConfirmed(any());
    }

    @Test
    void testVerifyPayment_Failure() {
        VerifyPaymentRequest req = new VerifyPaymentRequest();
        req.setOrderId(1L);

        when(orderRepository.findByOrderIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentClient.verifyPayment(anyMap())).thenReturn(Map.of("status", "FAILED", "message", "Failed"));

        OrderResponse resp = orderService.verifyPayment(1L, req);

        assertEquals(OrderStatus.FAILED, resp.getStatus());
    }

    @Test
    void testPlaceOrder_CardPayment() {
        Map<String, Object> cartResponse = new HashMap<>();
        cartResponse.put("items", List.of(Map.of("bookId", 10, "price", 100.0, "quantity", 1)));

        placeOrderRequest.setPaymentMode(PaymentMode.CARD);
        
        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentClient.createPaymentOrder(anyMap())).thenReturn(Map.of(
            "paymentId", 100L,
            "razorpayOrderId", "order_rzp_123",
            "razorpayKeyId", "key_123",
            "status", "CREATED"
        ));

        OrderResponse response = orderService.placeOrder(1L, placeOrderRequest);

        assertEquals("order_rzp_123", response.getRazorpayOrderId());
        verify(paymentClient).createPaymentOrder(anyMap());
    }

    @Test
    void testGetAllOrdersForAdmin() {
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(order));
        List<OrderResponse> responses = orderService.getAllOrdersForAdmin();
        assertFalse(responses.isEmpty());
        assertEquals(1L, responses.get(0).getOrderId());
    }

    @Test
    void testGetOrdersByUserId() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));
        List<OrderResponse> responses = orderService.getOrdersByUserId(1L);
        assertFalse(responses.isEmpty());
    }

    @Test
    void testPlaceOrder_EmptyCart() {
        when(cartClient.getCartByUserId(1L)).thenReturn(Map.of("items", List.of()));
        assertThrows(RuntimeException.class, () -> orderService.placeOrder(1L, placeOrderRequest));
    }

    @Test
    void testPlaceOrder_ZeroTotal() {
        Map<String, Object> cartResponse = Map.of("items", List.of(Map.of("bookId", 10, "price", 0, "quantity", 1)));
        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        assertThrows(RuntimeException.class, () -> orderService.placeOrder(1L, placeOrderRequest));
    }

    @Test
    void testPlaceOrder_COD_Success() {
        Map<String, Object> cartResponse = Map.of("items", List.of(Map.of("bookId", 10, "price", 100, "quantity", 1)));
        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        placeOrderRequest.setPaymentMode(PaymentMode.COD);
        OrderResponse resp = orderService.placeOrder(1L, placeOrderRequest);
        assertEquals(OrderStatus.CONFIRMED, resp.getStatus());
        verify(cartClient).clearCart(1L);
    }

    @Test
    void testPlaceOrder_InactiveCoupon() {
        Map<String, Object> cartResponse = Map.of("items", List.of(Map.of("bookId", 10, "price", 1000, "quantity", 1)));
        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        
        com.booknest.order.entity.Coupon coupon = new com.booknest.order.entity.Coupon();
        coupon.setActive(false);
        when(couponRepository.findByCode("INACTIVE")).thenReturn(Optional.of(coupon));
        
        placeOrderRequest.setCouponCode("INACTIVE");
        assertThrows(RuntimeException.class, () -> orderService.placeOrder(1L, placeOrderRequest));
    }

    @Test
    void testGetInvoicePdf_AddressFailure() throws Exception {
        order.setShippingAddressId(5L);
        when(orderRepository.findByOrderIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));
        when(authInternalClient.getAddress(any(), anyInt())).thenThrow(new RuntimeException("Auth Down"));
        when(pdfDocumentService.buildInvoicePdf(any(), any())).thenReturn(new byte[]{1});

        byte[] pdf = orderService.getInvoicePdf(1L, 1L);
        assertNotNull(pdf);
    }

    @Test
    void testUpdateOrderStatus_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(99L, OrderStatus.CANCELLED));
    }

    @Test
    void testPrivateHelpers_Conversions() {
        // We use Reflection or call methods that trigger these
        // But since they are used in placeOrder, we can trigger them there
        Map<String, Object> cartResponse = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("bookId", "10"); // String instead of Long
        item1.put("price", "100.0"); // String instead of BigDecimal
        item1.put("qty", "2"); // String instead of Integer
        items.add(item1);
        cartResponse.put("items", items);

        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.placeOrder(1L, placeOrderRequest);
        assertNotNull(response);
    }

    @Test
    void testPlaceOrder_BookDetailsFetch() {
        Map<String, Object> cartResponse = Map.of("items", List.of(Map.of("bookId", 10, "price", 100, "qty", 1)));
        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        // This will trigger bookClient.getBookById because title is missing
        when(bookClient.getBookById(10)).thenReturn(Map.of("title", "Remote Title", "author", "Remote Author"));

        OrderResponse resp = orderService.placeOrder(1L, placeOrderRequest);
        assertNotNull(resp);
        verify(bookClient).getBookById(10);
    }

    @Test
    void testPlaceOrder_FeignException() {
        Map<String, Object> cartResponse = Map.of("items", List.of(Map.of("bookId", 10, "price", 100, "qty", 1)));
        when(cartClient.getCartByUserId(1L)).thenReturn(cartResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        placeOrderRequest.setPaymentMode(PaymentMode.CARD);
        
        // Mocking FeignException is tricky, usually we throw a subclass
        feign.FeignException ex = mock(feign.FeignException.class);
        when(ex.contentUTF8()).thenReturn("{\"message\":\"Payment Gateway Down\"}");
        when(paymentClient.createPaymentOrder(anyMap())).thenThrow(ex);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> orderService.placeOrder(1L, placeOrderRequest));
        assertTrue(thrown.getMessage().contains("Payment Gateway Down"));
    }

    @Test
    void testReduceStock_Failure() {
        // This is a private method called via placeOrder or updateOrderStatus
        // We'll trigger it via updateOrderStatus
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        doThrow(new RuntimeException("Stock error")).when(bookClient).updateStock(anyInt(), anyInt());
        
        // Should not throw exception as it's caught inside reduceStockForOrder
        assertDoesNotThrow(() -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED));
    }

    @Test
    void testExtractCartItems_InvalidData() {
        // Trigger extractCartItems with non-list data
        when(cartClient.getCartByUserId(1L)).thenReturn(Map.of("items", "not-a-list"));
        assertThrows(RuntimeException.class, () -> orderService.placeOrder(1L, placeOrderRequest));
    }
}
