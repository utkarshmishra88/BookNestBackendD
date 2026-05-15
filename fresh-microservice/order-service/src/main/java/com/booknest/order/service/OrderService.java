package com.booknest.order.service;

import com.booknest.order.dto.OrderResponse;
import com.booknest.order.dto.PlaceOrderRequest;
import com.booknest.order.dto.VerifyPaymentRequest;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(Long userId, PlaceOrderRequest request);
    OrderResponse verifyPayment(Long userId, VerifyPaymentRequest request);
    List<OrderResponse> getOrdersByUserId(Long userId);
    OrderResponse getOrderById(Long userId, Long orderId);

    List<OrderResponse> getAllOrdersForAdmin();
    OrderResponse updateOrderStatus(Long orderId, com.booknest.order.enums.OrderStatus status);

    byte[] getInvoicePdf(Long userId, Long orderId);
    byte[] getReceiptPdf(Long userId, Long orderId);
}