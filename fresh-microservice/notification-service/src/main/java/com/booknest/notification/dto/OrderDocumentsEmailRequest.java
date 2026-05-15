package com.booknest.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDocumentsEmailRequest {
    private String toEmail;
    private String subject;
    /** HTML body */
    private String htmlBody;
    private String invoicePdfBase64;
    private String receiptPdfBase64;
    private String mobileNumber;
}
