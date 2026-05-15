package com.booknest.payment.dto;

import com.booknest.payment.enums.PaymentMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    @NotNull
    private Long orderId;

    @NotNull
    private Long userId;

    @NotNull
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    private BigDecimal amount;

    @NotNull
    private PaymentMode paymentMode;
}