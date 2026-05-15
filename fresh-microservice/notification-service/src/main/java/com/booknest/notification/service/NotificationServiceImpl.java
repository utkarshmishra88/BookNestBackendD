package com.booknest.notification.service;

import com.booknest.notification.entity.NotificationLog;
import com.booknest.notification.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationLogRepository repository;
    private final com.booknest.notification.client.AuthClient authClient;

    @org.springframework.beans.factory.annotation.Value("${booknest.internal-api-key:}")
    private String internalApiKey;

    @Override
    @Async
    public void sendOtpEmail(String toEmail, String otp, String mobileNumber) {
        String subject = "BookNest - Verify Your Registration";
        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family:'Inter', Arial, sans-serif; background-color:#f4f4f5; margin:0; padding:40px 0;">
                <table align="center" width="100%%" style="max-width:600px; background-color:#ffffff; border-radius:12px; box-shadow:0 4px 6px -1px rgba(0, 0, 0, 0.1); overflow:hidden; border:1px solid #e4e4e7; margin:auto; border-collapse: collapse;">
                    <tr>
                        <td style="padding:40px; text-align:center; background: linear-gradient(135deg, #4f46e5 0%%, #7c3aed 100%%);">
                            <h1 style="color:#ffffff; margin:0; font-size:28px; font-weight:700; letter-spacing:-0.5px;">BookNest</h1>
                            <p style="color:#e0e7ff; margin:10px 0 0 0; font-size:16px;">Verify your registration</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:40px 40px 20px 40px; text-align:center;">
                            <p style="color:#3f3f46; font-size:16px; line-height:1.6; margin:0;">Welcome to BookNest! Please use the following One-Time Password (OTP) to complete your registration process.</p>
                            <div style="margin:30px 0; padding:20px; background-color:#f8fafc; border-radius:8px; border:1px dashed #cbd5e1;">
                                <span style="font-family:monospace; font-size:36px; font-weight:700; color:#4f46e5; letter-spacing:8px;">%s</span>
                            </div>
                            <p style="color:#71717a; font-size:14px; margin:0;">This OTP is valid for <strong>5 minutes</strong>. Please do not share this code with anyone.</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:20px 40px 40px 40px; text-align:center; border-top:1px solid #f4f4f5;">
                            <p style="color:#a1a1aa; font-size:12px; margin:0;">© 2026 BookNest Inc. All rights reserved.</p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(otp);
        sendHtmlAndLog(toEmail, "OTP", subject, htmlBody, mobileNumber);
    }

    @Override
    @Async
    public void sendUpdateEmail(String toEmail, String subject, String message, String mobileNumber) {
        String body = "Hello,\n\nWe have an update regarding your BookNest account:\n\n" + message + 
                      "\n\nThank you for choosing BookNest!";
        sendAndLog(toEmail, "UPDATE", subject, body, mobileNumber);
    }

    @Override
    @Async
    public void sendOrderDocumentsEmail(String toEmail, String subject, String htmlBody,
                                        byte[] invoicePdf, byte[] receiptPdf, String mobileNumber) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (invoicePdf != null && invoicePdf.length > 0) {
                helper.addAttachment("BookNest-Order-Invoice.pdf", new ByteArrayResource(invoicePdf));
            }
            if (receiptPdf != null && receiptPdf.length > 0) {
                helper.addAttachment("BookNest-Payment-Receipt.pdf", new ByteArrayResource(receiptPdf));
            }
            mailSender.send(message);

            repository.save(NotificationLog.builder()
                    .recipientEmail(toEmail)
                    .mobileNumber(mobileNumber)
                    .type("ORDER_DOCS")
                    .subject(subject)
                    .messageBody(htmlBody != null ? htmlBody.substring(0, Math.min(htmlBody.length(), 2000)) : "")
                    .build());
        } catch (Exception e) {
            System.err.println("Error sending order documents email: " + e.getMessage());
        }

    }
    @Override
    @Async
    public void sendBroadcast(String subject, String messageText) {
        try {
            System.out.println("Initiating broadcast: " + subject);
            java.util.List<java.util.Map<String, String>> users = authClient.getAllUserContacts(internalApiKey);
            
            if (users == null || users.isEmpty()) {
                System.err.println("Broadcast aborted: No active users found in the database.");
                return;
            }

            System.out.println("Found " + users.size() + " active users. Sending emails...");

            String htmlTemplate = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family:'Inter', Arial, sans-serif; background-color:#f4f4f5; margin:0; padding:40px 0;">
                    <table align="center" width="100%%" style="max-width:640px; background-color:#ffffff; border-radius:12px; box-shadow:0 10px 15px -3px rgba(0, 0, 0, 0.1); overflow:hidden; border:1px solid #e4e4e7; margin:auto; border-collapse: collapse;">
                        <tr>
                            <td style="padding:40px; text-align:center; background: linear-gradient(135deg, #f59e0b 0%%, #d97706 100%%);">
                                <h1 style="color:#ffffff; margin:0; font-size:28px; font-weight:700; letter-spacing:-0.5px;">BookNest Announcement</h1>
                                <p style="color:#fef3c7; margin:10px 0 0 0; font-size:16px;">New Update from Admin</p>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:40px; text-align:left;">
                                <h2 style="color:#0f172a; font-size:20px; margin:0 0 15px 0;">Important Update</h2>
                                <div style="color:#475569; font-size:16px; line-height:1.7; margin-bottom:25px; white-space: pre-wrap;">%s</div>
                                <div style="text-align:center; padding-top:10px;">
                                    <a href="http://localhost:5173" style="display:inline-block; padding:12px 24px; background-color:#d97706; color:#ffffff; text-decoration:none; border-radius:8px; font-weight:600; font-size:14px;">Visit BookNest</a>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:20px 40px; text-align:center; background-color:#f8fafc; border-top:1px solid #f1f5f9;">
                                <p style="color:#94a3b8; font-size:12px; margin:0;">You are receiving this as a registered user of BookNest.</p>
                                <p style="color:#cbd5e1; font-size:11px; margin:10px 0 0 0;">© 2026 BookNest Inc. All rights reserved.</p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(messageText);

            int count = 0;
            for (java.util.Map<String, String> u : users) {
                String email = u.get("email");
                if (email != null && !email.isBlank()) {
                    sendHtmlAndLog(email, "BROADCAST", subject, htmlTemplate, null);
                    count++;
                }
            }
            System.out.println("Broadcast completed. Total emails sent: " + count);
        } catch (Exception e) {
            System.err.println("Broadcast failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendHtmlAndLog(String to, String type, String subject, String htmlBody, String mobileNumber) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);

            repository.save(NotificationLog.builder()
                    .recipientEmail(to)
                    .mobileNumber(mobileNumber)
                    .type(type)
                    .subject(subject)
                    .messageBody(htmlBody != null ? htmlBody.substring(0, Math.min(htmlBody.length(), 2000)) : "")
                    .build());
        } catch (Exception e) {
            System.err.println("Error sending HTML email: " + e.getMessage());
        }
    }

    private void sendAndLog(String to, String type, String subject, String message, String mobileNumber) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailSender.send(mailMessage);

            repository.save(NotificationLog.builder()
                    .recipientEmail(to)
                    .mobileNumber(mobileNumber)
                    .type(type)
                    .subject(subject)
                    .messageBody(message != null ? message.substring(0, Math.min(message.length(), 2000)) : "")
                    .build());
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}