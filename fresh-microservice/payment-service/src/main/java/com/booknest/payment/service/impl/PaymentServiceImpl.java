package com.booknest.payment.service.impl;

import com.booknest.payment.dto.*;
import com.booknest.payment.entity.Payment;
import com.booknest.payment.enums.PaymentMode;
import com.booknest.payment.enums.PaymentStatus;
import com.booknest.payment.exception.ResourceNotFoundException;
import com.booknest.payment.repository.PaymentRepository;
import com.booknest.payment.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final String razorpayKeyId;
    private final String razorpayKeySecret;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            RazorpayClient razorpayClient,
            @Value("${razorpay.key-id}") String razorpayKeyId,
            @Value("${razorpay.key-secret}") String razorpayKeySecret
    ) {
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
    }

    @Override
    public CreatePaymentResponse createPaymentOrder(CreatePaymentRequest request) {
        try {
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .userId(request.getUserId())
                    .amount(request.getAmount())
                    .paymentMode(request.getPaymentMode())
                    .status(PaymentStatus.CREATED)
                    .build();

            if (request.getPaymentMode() == PaymentMode.COD) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment = paymentRepository.save(payment);
                return CreatePaymentResponse.builder()
                        .paymentId(payment.getPaymentId())
                        .status(payment.getStatus())
                        .message("COD payment marked successful")
                        .build();
            }

            JSONObject options = new JSONObject();
            options.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            options.put("currency", "INR");
            options.put("receipt", "order_" + request.getOrderId() + "_user_" + request.getUserId());

            Order razorOrder = razorpayClient.orders.create(options);
            payment.setGatewayOrderId(razorOrder.get("id"));
            payment = paymentRepository.save(payment);

            return CreatePaymentResponse.builder()
                    .paymentId(payment.getPaymentId())
                    .razorpayOrderId(payment.getGatewayOrderId())
                    .razorpayKeyId(razorpayKeyId)
                    .status(payment.getStatus())
                    .message("Razorpay order created")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create payment order: " + e.getMessage(), e);
        }
    }

    @Override
    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        try {
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            String generated = hmacSha256Hex(payload, razorpayKeySecret);

            String sig = request.getRazorpaySignature();
            boolean valid = sig != null && generated.equalsIgnoreCase(sig);

            if (valid) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setGatewayPaymentId(request.getRazorpayPaymentId());
                payment.setFailureReason(null);
                paymentRepository.save(payment);
                return VerifyPaymentResponse.builder()
                        .paymentId(payment.getPaymentId())
                        .status(payment.getStatus())
                        .message("Payment verified successfully")
                        .build();
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Signature verification failed");
                paymentRepository.save(payment);
                return VerifyPaymentResponse.builder()
                        .paymentId(payment.getPaymentId())
                        .status(payment.getStatus())
                        .message("Payment verification failed")
                        .build();
            }
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);
            throw new RuntimeException("Failed to verify payment: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentDetailsResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return toDetails(payment);
    }

    @Override
    public List<PaymentDetailsResponse> listAllForAdmin() {
        return paymentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDetails)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDetailsResponse updateStatusForAdmin(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(status);
        paymentRepository.save(payment);
        return toDetails(payment);
    }

    private PaymentDetailsResponse toDetails(Payment payment) {
        return PaymentDetailsResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMode(payment.getPaymentMode())
                .status(payment.getStatus())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    /** Razorpay expects HMAC-SHA256 as lowercase hex (not Base64). */
    private String hmacSha256Hex(String data, String secret) throws Exception {
        Mac sha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256.init(secretKey);
        byte[] hash = sha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}