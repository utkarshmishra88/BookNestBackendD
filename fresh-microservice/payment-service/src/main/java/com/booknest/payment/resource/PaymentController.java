package com.booknest.payment.resource;

import com.booknest.payment.dto.*;
import com.booknest.payment.enums.PaymentStatus;
import com.booknest.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDetailsResponse>> listAllAdmin() {
        return ResponseEntity.ok(paymentService.listAllForAdmin());
    }

    @PatchMapping("/admin/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDetailsResponse> patchStatus(
            @PathVariable Long paymentId,
            @RequestBody Map<String, String> body) {
        String raw = body != null ? body.get("status") : null;
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        PaymentStatus st = PaymentStatus.valueOf(raw.trim().toUpperCase());
        return ResponseEntity.ok(paymentService.updateStatusForAdmin(paymentId, st));
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreatePaymentResponse> createOrder(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPaymentOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponse> verify(@Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailsResponse> getById(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }
}