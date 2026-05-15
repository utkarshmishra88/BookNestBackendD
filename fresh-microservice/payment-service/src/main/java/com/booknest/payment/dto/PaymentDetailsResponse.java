package com.booknest.payment.dto;

import com.booknest.payment.enums.PaymentMode;
import com.booknest.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentDetailsResponse {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private PaymentStatus status;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private String failureReason;
    private LocalDateTime createdAt;
}