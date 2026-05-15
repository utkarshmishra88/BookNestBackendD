package com.booknest.notification.service;

public interface NotificationService {
    void sendOtpEmail(String toEmail, String otp, String mobileNumber);
    void sendUpdateEmail(String toEmail, String subject, String message, String mobileNumber);

    void sendOrderDocumentsEmail(String toEmail, String subject, String htmlBody,
                                 byte[] invoicePdf, byte[] receiptPdf, String mobileNumber);

    void sendBroadcast(String subject, String message);
}