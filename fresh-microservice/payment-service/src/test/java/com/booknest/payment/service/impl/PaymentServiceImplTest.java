package com.booknest.payment.service.impl;

import com.booknest.payment.dto.*;
import com.booknest.payment.entity.Payment;
import com.booknest.payment.enums.PaymentMode;
import com.booknest.payment.enums.PaymentStatus;
import com.booknest.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RazorpayClient razorpayClient;

    private PaymentServiceImpl paymentService;

    private Payment payment;
    private CreatePaymentRequest createRequest;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, razorpayClient, "testId", "testSecret");

        payment = Payment.builder()
                .paymentId(1L)
                .orderId(10L)
                .userId(100L)
                .amount(BigDecimal.valueOf(500))
                .paymentMode(PaymentMode.UPI)
                .status(PaymentStatus.CREATED)
                .build();

        createRequest = new CreatePaymentRequest();
        createRequest.setOrderId(10L);
        createRequest.setUserId(100L);
        createRequest.setAmount(BigDecimal.valueOf(500));
        createRequest.setPaymentMode(PaymentMode.UPI);
    }

    @Test
    void testCreatePaymentOrder_COD() {
        createRequest.setPaymentMode(PaymentMode.COD);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        payment.setStatus(PaymentStatus.SUCCESS);

        CreatePaymentResponse response = paymentService.createPaymentOrder(createRequest);

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testVerifyPayment_NotFound() {
        VerifyPaymentRequest req = new VerifyPaymentRequest();
        req.setPaymentId(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.verifyPayment(req));
    }

    @Test
    void testGetPaymentById_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        PaymentDetailsResponse response = paymentService.getPaymentById(1L);
        assertEquals(1L, response.getPaymentId());
    }

    @Test
    void testListAllForAdmin() {
        when(paymentRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(payment));
        List<PaymentDetailsResponse> list = paymentService.listAllForAdmin();
        assertEquals(1, list.size());
    }

    @Test
    void testUpdateStatusForAdmin() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDetailsResponse response = paymentService.updateStatusForAdmin(1L, PaymentStatus.SUCCESS);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        verify(paymentRepository, times(1)).save(payment);
    }
}
