package com.booknest.payment.dto;

import com.booknest.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentResponse {
    private Long paymentId;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private PaymentStatus status;
    private String message;
}