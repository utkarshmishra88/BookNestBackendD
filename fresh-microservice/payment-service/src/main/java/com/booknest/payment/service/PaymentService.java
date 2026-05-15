package com.booknest.payment.service;

import com.booknest.payment.dto.*;

public interface PaymentService {
    CreatePaymentResponse createPaymentOrder(CreatePaymentRequest request);
    VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request);
    PaymentDetailsResponse getPaymentById(Long paymentId);

    java.util.List<PaymentDetailsResponse> listAllForAdmin();

    PaymentDetailsResponse updateStatusForAdmin(Long paymentId, com.booknest.payment.enums.PaymentStatus status);
}