package com.booknest.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyTopUpRequest {

    @NotNull
    private Long paymentId;

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpaySignature;
}