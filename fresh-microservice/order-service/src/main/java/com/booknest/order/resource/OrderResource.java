package com.booknest.order.resource;

import com.booknest.order.dto.OrderResponse;
import com.booknest.order.dto.PlaceOrderRequest;
import com.booknest.order.dto.VerifyPaymentRequest;
import com.booknest.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderResource {

    private final OrderService orderService;

    public OrderResource(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> adminListAll() {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin());
    }

    @PostMapping("/place/{userId}")
    public ResponseEntity<OrderResponse> placeOrder(@PathVariable Long userId,
                                                    @Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(userId, request));
    }

    @PostMapping("/verify/{userId}")
    public ResponseEntity<OrderResponse> verifyPayment(@PathVariable Long userId,
                                                       @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(orderService.verifyPayment(userId, request));
    }

    @GetMapping("/{userId:[0-9]+}")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @GetMapping("/{userId:[0-9]+}/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long userId,
                                                  @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(userId, orderId));
    }

    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam com.booknest.order.enums.OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @GetMapping("/{userId:[0-9]+}/{orderId}/invoice")
    public ResponseEntity<byte[]> getInvoice(@PathVariable Long userId, @PathVariable Long orderId) {
        byte[] pdf = orderService.getInvoicePdf(userId, orderId);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=invoice_" + orderId + ".pdf")
                .body(pdf);
    }

    @GetMapping("/{userId:[0-9]+}/{orderId}/receipt")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long userId, @PathVariable Long orderId) {
        byte[] pdf = orderService.getReceiptPdf(userId, orderId);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=receipt_" + orderId + ".pdf")
                .body(pdf);
    }
}