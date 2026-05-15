package com.booknest.order.service;

import com.booknest.order.client.AuthInternalClient;
import com.booknest.order.entity.Order;
import com.booknest.order.enums.OrderStatus;
import com.booknest.order.messaging.OrderEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderConfirmationNotifierTest {

    @Mock
    private AuthInternalClient authInternalClient;
    @Mock
    private OrderPdfDocumentService pdfDocumentService;
    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderConfirmationNotifier notifier;

    private Order order;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notifier, "internalApiKey", "test-key");
        order = new Order();
        order.setOrderId(1L);
        order.setUserId(10L);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setShippingAddressId(5L);
    }

    @Test
    void testNotifyOrderConfirmed_Success() throws Exception {
        when(authInternalClient.getUserEmail(anyString(), anyInt())).thenReturn(Map.of("email", "test@ex.com"));
        when(authInternalClient.getAddress(anyString(), anyInt())).thenReturn(Map.of("city", "TestCity"));
        when(pdfDocumentService.buildInvoicePdf(any(), any())).thenReturn(new byte[]{1});
        when(pdfDocumentService.buildPaymentReceiptPdf(any())).thenReturn(new byte[]{2});
        when(pdfDocumentService.buildInteractiveHtml(any(), anyString())).thenReturn("<html></html>");

        notifier.notifyOrderConfirmed(order);

        verify(orderEventPublisher).publishOrderNotification(any(), any());
    }

    @Test
    void testNotifyOrderConfirmed_NoEmail() {
        when(authInternalClient.getUserEmail(anyString(), anyInt())).thenReturn(Map.of("email", ""));
        notifier.notifyOrderConfirmed(order);
        verify(orderEventPublisher, never()).publishOrderNotification(any(), any());
    }

    @Test
    void testNotifyOrderConfirmed_AddressError() throws Exception {
        when(authInternalClient.getUserEmail(anyString(), anyInt())).thenReturn(Map.of("email", "test@ex.com"));
        when(authInternalClient.getAddress(anyString(), anyInt())).thenThrow(new RuntimeException("Error"));
        when(pdfDocumentService.buildInvoicePdf(any(), isNull())).thenReturn(new byte[]{1});
        when(pdfDocumentService.buildPaymentReceiptPdf(any())).thenReturn(new byte[]{2});

        notifier.notifyOrderConfirmed(order);

        verify(orderEventPublisher).publishOrderNotification(any(), any());
    }

    @Test
    void testNotifyOrderStatusUpdated_Success() {
        order.setStatus(OrderStatus.SHIPPED);
        when(authInternalClient.getUserEmail(anyString(), anyInt())).thenReturn(Map.of("email", "test@ex.com"));
        when(pdfDocumentService.buildStatusUpdateHtml(any(), anyString(), any())).thenReturn("<html></html>");

        notifier.notifyOrderStatusUpdated(order);

        verify(orderEventPublisher).publishOrderNotification(any(), any());
    }

    @Test
    void testNotifyOrderStatusUpdated_PendingPayment() {
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        notifier.notifyOrderStatusUpdated(order);
        verify(orderEventPublisher, never()).publishOrderNotification(any(), any());
    }
}
