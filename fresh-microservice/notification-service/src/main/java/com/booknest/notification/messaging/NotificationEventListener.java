package com.booknest.notification.messaging;

import com.booknest.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMqConfig.ORDER_QUEUE)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        if (event == null || event.getToEmail() == null || event.getToEmail().isBlank()) {
            return;
        }
        byte[] invoice = event.getInvoicePdfBase64() != null
                ? Base64.getDecoder().decode(event.getInvoicePdfBase64())
                : new byte[0];
        byte[] receipt = event.getReceiptPdfBase64() != null
                ? Base64.getDecoder().decode(event.getReceiptPdfBase64())
                : new byte[0];

        notificationService.sendOrderDocumentsEmail(
                event.getToEmail(),
                event.getSubject() != null ? event.getSubject() : "Your BookNest order",
                event.getHtmlBody() != null ? event.getHtmlBody() : "<p>Thank you for your order.</p>",
                invoice,
                receipt,
                event.getMobileNumber()
        );
    }

    @RabbitListener(queues = RabbitMqConfig.AUTH_OTP_QUEUE)
    public void onAuthOtpRequested(AuthOtpRequestedEvent event) {
        if (event == null || event.getEmail() == null || event.getEmail().isBlank()) {
            return;
        }
        notificationService.sendOtpEmail(event.getEmail(), event.getOtp(), event.getMobileNumber());
    }

    @RabbitListener(queues = RabbitMqConfig.AUTH_UPDATE_QUEUE)
    public void onAuthUpdateRequested(AuthUpdateRequestedEvent event) {
        if (event == null || event.getEmail() == null || event.getEmail().isBlank()) {
            return;
        }
        notificationService.sendUpdateEmail(
                event.getEmail(),
                event.getSubject(),
                event.getMessage(),
                event.getMobileNumber()
        );
    }
}
