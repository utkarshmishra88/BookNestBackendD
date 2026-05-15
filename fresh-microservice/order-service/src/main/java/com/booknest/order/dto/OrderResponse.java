package com.booknest.order.dto;

import com.booknest.order.enums.OrderStatus;
import com.booknest.order.enums.PaymentMode;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    
    // No-arg constructor for builder
    public OrderResponse() {}
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private PaymentMode paymentMode;
    private OrderStatus status;
    private Long paymentId;
    private String paymentGatewayOrderId;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private String message;
    private LocalDateTime createdAt;
    private String couponCode;
    private BigDecimal discountAmount;
    private List<OrderItemResponse> items;

    // Explicit builder factory method (Lombok @Builder backup)
    public static OrderResponseBuilder builder() {
        return new OrderResponseBuilder();
    }

    public static class OrderResponseBuilder {
        private Long orderId;
        private Long userId;
        private BigDecimal totalAmount;
        private PaymentMode paymentMode;
        private OrderStatus status;
        private Long paymentId;
        private String paymentGatewayOrderId;
        private String razorpayOrderId;
        private String razorpayKeyId;
        private String message;
        private LocalDateTime createdAt;
        private String couponCode;
        private BigDecimal discountAmount;
        private List<OrderItemResponse> items;

        public OrderResponseBuilder orderId(Long orderId) { this.orderId = orderId; return this; }
        public OrderResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public OrderResponseBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public OrderResponseBuilder paymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; return this; }
        public OrderResponseBuilder status(OrderStatus status) { this.status = status; return this; }
        public OrderResponseBuilder paymentId(Long paymentId) { this.paymentId = paymentId; return this; }
        public OrderResponseBuilder paymentGatewayOrderId(String paymentGatewayOrderId) { this.paymentGatewayOrderId = paymentGatewayOrderId; return this; }
        public OrderResponseBuilder razorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; return this; }
        public OrderResponseBuilder razorpayKeyId(String razorpayKeyId) { this.razorpayKeyId = razorpayKeyId; return this; }
        public OrderResponseBuilder message(String message) { this.message = message; return this; }
        public OrderResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderResponseBuilder couponCode(String couponCode) { this.couponCode = couponCode; return this; }
        public OrderResponseBuilder discountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; return this; }
        public OrderResponseBuilder items(List<OrderItemResponse> items) { this.items = items; return this; }

        public OrderResponse build() {
            OrderResponse response = new OrderResponse();
            response.orderId = this.orderId;
            response.userId = this.userId;
            response.totalAmount = this.totalAmount;
            response.paymentMode = this.paymentMode;
            response.status = this.status;
            response.paymentId = this.paymentId;
            response.paymentGatewayOrderId = this.paymentGatewayOrderId;
            response.razorpayOrderId = this.razorpayOrderId;
            response.razorpayKeyId = this.razorpayKeyId;
            response.message = this.message;
            response.createdAt = this.createdAt;
            response.couponCode = this.couponCode;
            response.discountAmount = this.discountAmount;
            response.items = this.items;
            return response;
        }
    }
}