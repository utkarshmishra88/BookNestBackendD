package com.booknest.order.service;

import com.booknest.order.client.AuthInternalClient;
import com.booknest.order.dto.OrderDocumentsEmailDto;
import com.booknest.order.entity.Order;
import com.booknest.order.enums.OrderStatus;
import com.booknest.order.messaging.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderConfirmationNotifier {

    private final AuthInternalClient authInternalClient;
    private final OrderPdfDocumentService pdfDocumentService;
    private final OrderEventPublisher orderEventPublisher;

    @Value("${booknest.internal-api-key:}")
    private String internalApiKey;

    public void notifyOrderConfirmed(Order order) {
        if (order == null || order.getStatus() != OrderStatus.CONFIRMED) {
            return;
        }
        try {
            Map<String, String> emailMap = authInternalClient.getUserEmail(internalApiKey, order.getUserId().intValue());
            String email = emailMap != null ? emailMap.get("email") : null;
            String mobileNumber = emailMap != null ? emailMap.get("mobileNumber") : null;
            if (email == null || email.isBlank()) {
                return;
            }
            byte[] invoice = pdfDocumentService.buildInvoicePdf(order, null);
            if (order.getShippingAddressId() != null) {
                try {
                    Map<String, Object> addressMap = authInternalClient.getAddress(internalApiKey, order.getShippingAddressId().intValue());
                    invoice = pdfDocumentService.buildInvoicePdf(order, addressMap);
                } catch (Exception e) {
                    System.err.println("Could not fetch address for PDF: " + e.getMessage());
                }
            }
            byte[] receipt = pdfDocumentService.buildPaymentReceiptPdf(order);
            String html = pdfDocumentService.buildInteractiveHtml(order, email);
            OrderDocumentsEmailDto dto = OrderDocumentsEmailDto.builder()
                    .toEmail(email)
                    .subject("Your BookNest order #" + order.getOrderId() + " — invoice & receipt")
                    .htmlBody(html)
                    .invoicePdfBase64(Base64.getEncoder().encodeToString(invoice))
                    .receiptPdfBase64(Base64.getEncoder().encodeToString(receipt))
                    .mobileNumber(mobileNumber)
                    .build();
            orderEventPublisher.publishOrderNotification(order, dto);
        } catch (Exception e) {
            System.err.println("Order confirmation email failed: " + e.getMessage());
        }
    }

    public void notifyOrderStatusUpdated(Order order) {
        if (order == null || order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            return; // Don't send status updates for PENDING_PAYMENT
        }
        try {
            Map<String, String> emailMap = authInternalClient.getUserEmail(internalApiKey, order.getUserId().intValue());
            String email = emailMap != null ? emailMap.get("email") : null;
            String mobileNumber = emailMap != null ? emailMap.get("mobileNumber") : null;
            if (email == null || email.isBlank()) {
                return;
            }
            
            String statusText = order.getStatus().name().substring(0, 1) + order.getStatus().name().substring(1).toLowerCase().replace("_", " ");
            String html = pdfDocumentService.buildStatusUpdateHtml(order, email, order.getStatus());
            OrderDocumentsEmailDto dto = OrderDocumentsEmailDto.builder()
                    .toEmail(email)
                    .subject("BookNest order #" + order.getOrderId() + " update: " + statusText)
                    .htmlBody(html)
                    .invoicePdfBase64(null)
                    .receiptPdfBase64(null)
                    .mobileNumber(mobileNumber)
                    .build();
            orderEventPublisher.publishOrderNotification(order, dto);
        } catch (Exception e) {
            System.err.println("Order status update email failed: " + e.getMessage());
        }
    }
}
