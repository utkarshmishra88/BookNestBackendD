package com.booknest.order.service;

import com.booknest.order.entity.Order;
import com.booknest.order.entity.OrderItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Color;

@Service
public class OrderPdfDocumentService {

    public byte[] buildInvoicePdf(Order order, java.util.Map<String, Object> addressMap) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        doc.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, new Color(15, 23, 42));
        Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(71, 85, 105));
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(51, 65, 85));
        Font bodyBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(15, 23, 42));
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(16, 185, 129));

        // Header
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 1});
        
        PdfPCell logoCell = new PdfPCell(new Phrase("BookNest", headerFont));
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTable.addCell(logoCell);

        PdfPCell invoiceTextCell = new PdfPCell(new Phrase("INVOICE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, new Color(226, 232, 240))));
        invoiceTextCell.setBorder(Rectangle.NO_BORDER);
        invoiceTextCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        invoiceTextCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(invoiceTextCell);
        
        doc.add(headerTable);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));

        // Order details
        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Phrase("Invoice To:", subHeaderFont));
        leftCell.addElement(new Phrase("Customer ID: " + order.getUserId(), bodyFont));
        if (addressMap != null && !addressMap.isEmpty()) {
            leftCell.addElement(new Phrase((String) addressMap.get("line1"), bodyFont));
            if (addressMap.get("line2") != null && !((String) addressMap.get("line2")).isBlank()) {
                leftCell.addElement(new Phrase((String) addressMap.get("line2"), bodyFont));
            }
            String cityState = addressMap.get("city") + ", " + addressMap.get("state") + " " + addressMap.get("postalCode");
            leftCell.addElement(new Phrase(cityState, bodyFont));
            leftCell.addElement(new Phrase((String) addressMap.get("country"), bodyFont));
            if (addressMap.get("mobileNumber") != null) {
                leftCell.addElement(new Phrase("Phone: " + addressMap.get("mobileNumber"), bodyFont));
            }
        }
        detailsTable.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph p1 = new Paragraph("Order #: ", bodyBold); p1.add(new Chunk(String.valueOf(order.getOrderId()), bodyFont));
        Paragraph p2 = new Paragraph("Date: ", bodyBold); p2.add(new Chunk(String.valueOf(order.getCreatedAt()), bodyFont));
        Paragraph p3 = new Paragraph("Status: ", bodyBold); p3.add(new Chunk(String.valueOf(order.getStatus()), bodyFont));
        p1.setAlignment(Element.ALIGN_RIGHT); p2.setAlignment(Element.ALIGN_RIGHT); p3.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(p1); rightCell.addElement(p2); rightCell.addElement(p3);
        detailsTable.addCell(rightCell);
        
        doc.add(detailsTable);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));

        // Items Table
        PdfPTable itemsTable = new PdfPTable(4);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{4, 1, 1.5f, 1.5f});
        
        Color tableHeaderBg = new Color(241, 245, 249);
        String[] headers = {"Item Description", "Qty", "Price", "Total"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, bodyBold));
            cell.setBackgroundColor(tableHeaderBg);
            cell.setPadding(8);
            cell.setBorderColor(new Color(226, 232, 240));
            if (!h.equals("Item Description")) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            if (h.equals("Total")) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            itemsTable.addCell(cell);
        }

        for (OrderItem it : order.getItems()) {
            PdfPCell c1 = new PdfPCell(new Phrase(it.getTitle(), bodyFont));
            c1.setPadding(8); c1.setBorderColor(new Color(226, 232, 240));
            itemsTable.addCell(c1);

            PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(it.getQuantity()), bodyFont));
            c2.setPadding(8); c2.setBorderColor(new Color(226, 232, 240));
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(c2);

            PdfPCell c3 = new PdfPCell(new Phrase("Rs " + it.getPrice(), bodyFont));
            c3.setPadding(8); c3.setBorderColor(new Color(226, 232, 240));
            c3.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(c3);

            PdfPCell c4 = new PdfPCell(new Phrase("Rs " + it.getLineTotal(), bodyFont));
            c4.setPadding(8); c4.setBorderColor(new Color(226, 232, 240));
            c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
            itemsTable.addCell(c4);
        }
        doc.add(itemsTable);

        // Total
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setWidths(new float[]{7.5f, 1.5f});
        
        PdfPCell blankCell = new PdfPCell(new Phrase(""));
        blankCell.setBorder(Rectangle.NO_BORDER);
        totalTable.addCell(blankCell);

        PdfPCell totalCell = new PdfPCell(new Phrase("Total: Rs " + order.getTotalAmount(), totalFont));
        totalCell.setBorder(Rectangle.NO_BORDER);
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalCell.setPaddingTop(15);
        totalTable.addCell(totalCell);
        
        doc.add(totalTable);

        doc.close();
        return baos.toByteArray();
    }

    public byte[] buildPaymentReceiptPdf(Order order) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);
        doc.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, new Color(15, 23, 42));
        Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(71, 85, 105));
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(51, 65, 85));
        Font bodyBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(15, 23, 42));
        Font amountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(16, 185, 129));

        // Header
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 1});
        
        PdfPCell logoCell = new PdfPCell(new Phrase("BookNest", headerFont));
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTable.addCell(logoCell);

        PdfPCell receiptTextCell = new PdfPCell(new Phrase("RECEIPT", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, new Color(226, 232, 240))));
        receiptTextCell.setBorder(Rectangle.NO_BORDER);
        receiptTextCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        receiptTextCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(receiptTextCell);
        
        doc.add(headerTable);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));

        // Content
        PdfPTable detailsTable = new PdfPTable(1);
        detailsTable.setWidthPercentage(100);
        
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPadding(20);
        infoCell.setBackgroundColor(new Color(248, 250, 252));
        
        infoCell.addElement(new Paragraph("Payment Details", subHeaderFont));
        infoCell.addElement(new Paragraph(" "));
        infoCell.addElement(new Paragraph("Order Number: " + order.getOrderId(), bodyFont));
        infoCell.addElement(new Paragraph("Date: " + order.getCreatedAt(), bodyFont));
        infoCell.addElement(new Paragraph("Payment Method: " + order.getPaymentMode(), bodyFont));
        
        if (order.getPaymentId() != null) {
            infoCell.addElement(new Paragraph("Payment Reference: " + order.getPaymentId(), bodyFont));
        }
        if (order.getPaymentGatewayOrderId() != null) {
            infoCell.addElement(new Paragraph("Gateway Order ID: " + order.getPaymentGatewayOrderId(), bodyFont));
        }
        
        infoCell.addElement(new Paragraph(" "));
        Paragraph amtP = new Paragraph("Amount Paid: Rs " + order.getTotalAmount(), amountFont);
        amtP.setAlignment(Element.ALIGN_CENTER);
        infoCell.addElement(amtP);
        
        detailsTable.addCell(infoCell);
        doc.add(detailsTable);
        
        doc.add(new Paragraph(" "));
        Paragraph thx = new Paragraph("Thank you for your purchase!", bodyBold);
        thx.setAlignment(Element.ALIGN_CENTER);
        doc.add(thx);

        doc.close();
        return baos.toByteArray();
    }

    public String buildInteractiveHtml(Order order, String customerEmail) {
        StringBuilder rows = new StringBuilder();
        for (OrderItem it : order.getItems()) {
            rows.append("<tr><td style=\"border-bottom:1px solid #e2e8f0; padding:12px 0; color:#334155;\"><strong>")
                .append(escapeHtml(it.getTitle()))
                .append("</strong></td><td align=\"center\" style=\"border-bottom:1px solid #e2e8f0; padding:12px 0; color:#475569;\">")
                .append(it.getQuantity())
                .append("</td><td align=\"right\" style=\"border-bottom:1px solid #e2e8f0; padding:12px 0; color:#0f172a;\">₹")
                .append(it.getLineTotal())
                .append("</td></tr>");
        }
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family:'Inter', Arial, sans-serif; background-color:#f4f4f5; margin:0; padding:40px 0;">
                    <table align="center" width="100%%" style="max-width:640px; background-color:#ffffff; border-radius:12px; box-shadow:0 10px 15px -3px rgba(0, 0, 0, 0.1); overflow:hidden; border:1px solid #e4e4e7; margin:auto; border-collapse: collapse;">
                        <tr>
                            <td style="padding:40px; text-align:center; background: linear-gradient(135deg, #10b981 0%%, #059669 100%%);">
                                <h1 style="color:#ffffff; margin:0; font-size:28px; font-weight:700; letter-spacing:-0.5px;">BookNest</h1>
                                <p style="color:#d1fae5; margin:10px 0 0 0; font-size:16px;">Order Confirmation</p>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:40px; text-align:left;">
                                <h2 style="color:#0f172a; font-size:20px; margin:0 0 10px 0;">Hi %s,</h2>
                                <p style="color:#475569; font-size:16px; line-height:1.6; margin:0 0 20px 0;">Thank you for your purchase! We're getting your order ready. Your invoice and payment receipt are attached to this email.</p>
                                <div style="background-color:#f8fafc; border-radius:8px; padding:20px; margin-bottom:30px; border-left:4px solid #10b981;">
                                    <p style="margin:0; color:#334155; font-size:14px;"><strong>Order Number:</strong> #%s</p>
                                    <p style="margin:8px 0 0 0; color:#334155; font-size:14px;"><strong>Payment Method:</strong> %s</p>
                                </div>
                                <h3 style="color:#0f172a; font-size:18px; margin:0 0 15px 0; border-bottom:2px solid #e2e8f0; padding-bottom:10px;">Order Summary</h3>
                                <table width="100%%" cellpadding="0" style="border-collapse:collapse; font-size:14px; margin-bottom:20px;">
                                    <tr style="text-transform:uppercase; font-size:12px; color:#64748b; letter-spacing:0.5px;">
                                        <th align="left" style="padding-bottom:10px; border-bottom:2px solid #e2e8f0;">Item</th>
                                        <th align="center" style="padding-bottom:10px; border-bottom:2px solid #e2e8f0;">Qty</th>
                                        <th align="right" style="padding-bottom:10px; border-bottom:2px solid #e2e8f0;">Total</th>
                                    </tr>
                                    %s
                                </table>
                                <div style="text-align:right; padding-top:15px;">
                                    <p style="margin:0; font-size:20px; color:#0f172a;"><strong>Total: ₹%s</strong></p>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:20px 40px; text-align:center; background-color:#f8fafc; border-top:1px solid #f1f5f9;">
                                <p style="color:#94a3b8; font-size:13px; margin:0;">Need help? <a href="#" style="color:#10b981; text-decoration:none;">Contact Support</a></p>
                                <p style="color:#cbd5e1; font-size:12px; margin:10px 0 0 0;">© 2026 BookNest Inc. All rights reserved.</p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                escapeHtml(customerEmail != null ? customerEmail.split("@")[0] : "reader"),
                order.getOrderId(),
                order.getPaymentMode(),
                rows.toString(),
                order.getTotalAmount()
        );
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public String buildStatusUpdateHtml(Order order, String customerEmail, com.booknest.order.enums.OrderStatus newStatus) {
        String statusText = newStatus.name().substring(0, 1) + newStatus.name().substring(1).toLowerCase().replace("_", " ");
        String headerBg = "#3b82f6"; // default blue
        String headerGradient = "linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)";
        String message = "There is an update regarding your order.";

        if (newStatus == com.booknest.order.enums.OrderStatus.DELIVERED) {
            headerBg = "#10b981"; // green
            headerGradient = "linear-gradient(135deg, #10b981 0%, #059669 100%)";
            message = "Great news! Your order has been delivered successfully. We hope you enjoy your purchase.";
        } else if (newStatus == com.booknest.order.enums.OrderStatus.CANCELLED) {
            headerBg = "#ef4444"; // red
            headerGradient = "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)";
            message = "Your order has been cancelled. If a payment was made, it will be refunded according to our policy.";
        } else if (newStatus == com.booknest.order.enums.OrderStatus.FAILED) {
            headerBg = "#ef4444"; // red
            headerGradient = "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)";
            message = "Unfortunately, there was an issue processing your order and it has failed. Please contact support for assistance.";
        } else if (newStatus == com.booknest.order.enums.OrderStatus.SHIPPED) {
            headerBg = "#3b82f6"; // blue
            headerGradient = "linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)";
            message = "Great news! Your order has been shipped and is on its way to you.";
        }

        StringBuilder rows = new StringBuilder();
        for (OrderItem it : order.getItems()) {
            rows.append("<tr><td style=\"border-bottom:1px solid #e2e8f0; padding:12px 0; color:#334155;\"><strong>")
                .append(escapeHtml(it.getTitle()))
                .append("</strong></td><td align=\"center\" style=\"border-bottom:1px solid #e2e8f0; padding:12px 0; color:#475569;\">")
                .append(it.getQuantity())
                .append("</td></tr>");
        }
        
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family:'Inter', Arial, sans-serif; background-color:#f4f4f5; margin:0; padding:40px 0;">
                    <table align="center" width="100%%" style="max-width:640px; background-color:#ffffff; border-radius:12px; box-shadow:0 10px 15px -3px rgba(0, 0, 0, 0.1); overflow:hidden; border:1px solid #e4e4e7; margin:auto; border-collapse: collapse;">
                        <tr>
                            <td style="padding:40px; text-align:center; background: %s;">
                                <h1 style="color:#ffffff; margin:0; font-size:28px; font-weight:700; letter-spacing:-0.5px;">BookNest</h1>
                                <p style="color:#ffffff; opacity: 0.9; margin:10px 0 0 0; font-size:16px;">Order %s</p>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:40px; text-align:left;">
                                <h2 style="color:#0f172a; font-size:20px; margin:0 0 10px 0;">Hi %s,</h2>
                                <p style="color:#475569; font-size:16px; line-height:1.6; margin:0 0 20px 0;">%s</p>
                                <div style="background-color:#f8fafc; border-radius:8px; padding:20px; margin-bottom:30px; border-left:4px solid %s;">
                                    <p style="margin:0; color:#334155; font-size:14px;"><strong>Order Number:</strong> #%s</p>
                                    <p style="margin:8px 0 0 0; color:#334155; font-size:14px;"><strong>Status:</strong> %s</p>
                                </div>
                                <h3 style="color:#0f172a; font-size:18px; margin:0 0 15px 0; border-bottom:2px solid #e2e8f0; padding-bottom:10px;">Order Summary</h3>
                                <table width="100%%" cellpadding="0" style="border-collapse:collapse; font-size:14px; margin-bottom:20px;">
                                    <tr style="text-transform:uppercase; font-size:12px; color:#64748b; letter-spacing:0.5px;">
                                        <th align="left" style="padding-bottom:10px; border-bottom:2px solid #e2e8f0;">Item</th>
                                        <th align="center" style="padding-bottom:10px; border-bottom:2px solid #e2e8f0;">Qty</th>
                                    </tr>
                                    %s
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:20px 40px; text-align:center; background-color:#f8fafc; border-top:1px solid #f1f5f9;">
                                <p style="color:#94a3b8; font-size:13px; margin:0;">Need help? <a href="#" style="color:%s; text-decoration:none;">Contact Support</a></p>
                                <p style="color:#cbd5e1; font-size:12px; margin:10px 0 0 0;">© 2026 BookNest Inc. All rights reserved.</p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                headerGradient,
                statusText,
                escapeHtml(customerEmail != null ? customerEmail.split("@")[0] : "reader"),
                message,
                headerBg,
                order.getOrderId(),
                statusText,
                rows.toString(),
                headerBg
        );
    }
}
