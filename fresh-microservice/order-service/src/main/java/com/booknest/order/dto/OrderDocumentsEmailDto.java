package com.booknest.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDocumentsEmailDto {
    private String toEmail;
    private String subject;
    private String htmlBody;
    private String invoicePdfBase64;
    private String receiptPdfBase64;
    private String mobileNumber;
}
