package com.booknest.notification.resource;

import com.booknest.notification.dto.OrderDocumentsEmailRequest;
import com.booknest.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationResource {

    private final NotificationService notificationService;

    @Value("${booknest.internal-api-key:}")
    private String internalApiKey;

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam String email, @RequestParam String otp, @RequestParam(required = false) String mobileNumber) {
        notificationService.sendOtpEmail(email, otp, mobileNumber);
        return ResponseEntity.ok("OTP Email request accepted.");
    }

    @PostMapping("/send-update")
    public ResponseEntity<String> sendUpdate(
            @RequestParam String email, 
            @RequestParam String subject, 
            @RequestParam String message,
            @RequestParam(required = false) String mobileNumber) {
        notificationService.sendUpdateEmail(email, subject, message, mobileNumber);
        return ResponseEntity.ok("Update Email request accepted.");
    }

    /** Called by order-service with PDFs (invoice + receipt). */
    @PostMapping("/internal/order-documents")
    public ResponseEntity<String> sendOrderDocumentsInternal(
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String key,
            @RequestBody OrderDocumentsEmailRequest body) {
        if (internalApiKey == null || internalApiKey.isBlank() || !internalApiKey.equals(key)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        if (body.getToEmail() == null || body.getToEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Missing email");
        }
        byte[] inv = body.getInvoicePdfBase64() != null
                ? Base64.getDecoder().decode(body.getInvoicePdfBase64()) : new byte[0];
        byte[] rec = body.getReceiptPdfBase64() != null
                ? Base64.getDecoder().decode(body.getReceiptPdfBase64()) : new byte[0];
        notificationService.sendOrderDocumentsEmail(
                body.getToEmail(),
                body.getSubject() != null ? body.getSubject() : "Your BookNest order",
                body.getHtmlBody() != null ? body.getHtmlBody() : "<p>Thank you for your order.</p>",
                inv,
                rec,
                body.getMobileNumber()
        );
        return ResponseEntity.ok("Queued");
    }
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> sendBroadcast(
            @RequestBody java.util.Map<String, String> body) {
        String subject = body.get("subject");
        String message = body.get("message");
        if (subject == null || message == null) {
            return ResponseEntity.badRequest().body("Missing subject or message");
        }
        notificationService.sendBroadcast(subject, message);
        return ResponseEntity.ok("Broadcast request accepted.");
    }
}