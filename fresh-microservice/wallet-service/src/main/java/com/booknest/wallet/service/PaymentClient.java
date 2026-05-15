package com.booknest.wallet.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/payments/create-order")
    Map<String, Object> createPaymentOrder(@RequestBody Map<String, Object> request);

    @PostMapping("/payments/verify")
    Map<String, Object> verifyPayment(@RequestBody Map<String, Object> request);

    @GetMapping("/payments/{paymentId}")
    Map<String, Object> getPaymentById(@PathVariable("paymentId") Long paymentId);
}