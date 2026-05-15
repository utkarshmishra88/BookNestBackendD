package com.booknest.notification.messaging;

import com.booknest.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @Test
    void testOnOrderConfirmed() {
        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setToEmail("test@example.com");
        event.setInvoicePdfBase64(Base64.getEncoder().encodeToString(new byte[]{1}));
        event.setReceiptPdfBase64(Base64.getEncoder().encodeToString(new byte[]{2}));

        listener.onOrderConfirmed(event);

        verify(notificationService).sendOrderDocumentsEmail(
                eq("test@example.com"), any(), any(), any(), any(), any()
        );
    }

    @Test
    void testOnAuthOtpRequested() {
        AuthOtpRequestedEvent event = new AuthOtpRequestedEvent();
        event.setEmail("test@example.com");
        event.setOtp("123456");

        listener.onAuthOtpRequested(event);

        verify(notificationService).sendOtpEmail(eq("test@example.com"), eq("123456"), any());
    }

    @Test
    void testOnAuthUpdateRequested() {
        AuthUpdateRequestedEvent event = new AuthUpdateRequestedEvent();
        event.setEmail("test@example.com");
        event.setSubject("Subject");
        event.setMessage("Message");

        listener.onAuthUpdateRequested(event);

        verify(notificationService).sendUpdateEmail(eq("test@example.com"), eq("Subject"), eq("Message"), any());
    }

    @Test
    void testOnOrderConfirmed_NullEvent() {
        listener.onOrderConfirmed(null);
        verify(notificationService, never()).sendOrderDocumentsEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testOnAuthOtpRequested_EmptyEmail() {
        AuthOtpRequestedEvent event = new AuthOtpRequestedEvent();
        event.setEmail("");
        listener.onAuthOtpRequested(event);
        verify(notificationService, never()).sendOtpEmail(any(), any(), any());
    }

    @Test
    void testOnOrderConfirmed_EmptyEmail() {
        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setToEmail("");
        listener.onOrderConfirmed(event);
        verify(notificationService, never()).sendOrderDocumentsEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testOnOrderConfirmed_NullPdfs() {
        OrderConfirmedEvent event = new OrderConfirmedEvent();
        event.setToEmail("test@ex.com");
        event.setInvoicePdfBase64(null);
        event.setReceiptPdfBase64(null);
        event.setSubject(null);
        event.setHtmlBody(null);

        listener.onOrderConfirmed(event);

        verify(notificationService).sendOrderDocumentsEmail(
                eq("test@ex.com"), 
                eq("Your BookNest order"), 
                eq("<p>Thank you for your order.</p>"), 
                any(byte[].class), 
                any(byte[].class), 
                any()
        );
    }

    @Test
    void testOnAuthUpdateRequested_Null() {
        listener.onAuthUpdateRequested(null);
        verify(notificationService, never()).sendUpdateEmail(any(), any(), any(), any());
    }

    @Test
    void testOnAuthUpdateRequested_EmptyEmail() {
        AuthUpdateRequestedEvent event = new AuthUpdateRequestedEvent();
        event.setEmail(" ");
        listener.onAuthUpdateRequested(event);
        verify(notificationService, never()).sendUpdateEmail(any(), any(), any(), any());
    }
}
