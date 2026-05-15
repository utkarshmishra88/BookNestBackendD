package com.booknest.payment.resource;

import com.booknest.payment.dto.CreatePaymentRequest;
import com.booknest.payment.dto.CreatePaymentResponse;
import com.booknest.payment.dto.PaymentDetailsResponse;
import com.booknest.payment.dto.VerifyPaymentRequest;
import com.booknest.payment.dto.VerifyPaymentResponse;
import com.booknest.payment.enums.PaymentMode;
import com.booknest.payment.enums.PaymentStatus;
import com.booknest.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PaymentControllerTest {

    private PaymentDetailsResponse mockPaymentDetailsResponse() {
        return PaymentDetailsResponse.builder()
                .paymentId(1L)
                .orderId(10L)
                .userId(5L)
                .amount(java.math.BigDecimal.valueOf(499.0))
                .paymentMode(PaymentMode.UPI)
                .status(PaymentStatus.CREATED)
                .gatewayOrderId("order_123")
                .gatewayPaymentId("payment_123")
                .failureReason(null)
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listAllAdmin_ShouldReturnList() {
        when(paymentService.listAllForAdmin()).thenReturn(Collections.singletonList(mockPaymentDetailsResponse()));
        ResponseEntity<List<PaymentDetailsResponse>> result = paymentController.listAllAdmin();
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void patchStatus_ShouldThrowException_WhenStatusNull() {
        assertThrows(IllegalArgumentException.class, () -> paymentController.patchStatus(1L, null));
    }

    @Test
    void patchStatus_ShouldReturnUpdatedPayment() {
        PaymentDetailsResponse response = mockPaymentDetailsResponse();
        when(paymentService.updateStatusForAdmin(eq(1L), any(PaymentStatus.class))).thenReturn(response);

        Map<String, String> body = new HashMap<>();
        body.put("status", "SUCCESS");
        
        ResponseEntity<PaymentDetailsResponse> result = paymentController.patchStatus(1L, body);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        CreatePaymentResponse response = CreatePaymentResponse.builder()
                .paymentId(1L)
                .razorpayOrderId("order_123")
                .razorpayKeyId("rzp_test_key")
                .status(PaymentStatus.CREATED)
                .message("created")
                .build();
        when(paymentService.createPaymentOrder(any())).thenReturn(response);

        ResponseEntity<CreatePaymentResponse> result = paymentController.createOrder(request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void verify_ShouldReturnVerificationResponse() {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        VerifyPaymentResponse response = VerifyPaymentResponse.builder()
                .paymentId(1L)
                .status(PaymentStatus.SUCCESS)
                .message("verified")
                .build();
        when(paymentService.verifyPayment(any())).thenReturn(response);

        ResponseEntity<VerifyPaymentResponse> result = paymentController.verify(request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getById_ShouldReturnPayment() {
        PaymentDetailsResponse response = mockPaymentDetailsResponse();
        when(paymentService.getPaymentById(1L)).thenReturn(response);

        ResponseEntity<PaymentDetailsResponse> result = paymentController.getById(1L);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }
}
