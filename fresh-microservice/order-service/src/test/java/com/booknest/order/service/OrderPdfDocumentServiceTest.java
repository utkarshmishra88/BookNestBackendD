package com.booknest.order.service;

import com.booknest.order.entity.Order;
import com.booknest.order.entity.OrderItem;
import com.booknest.order.enums.OrderStatus;
import com.booknest.order.enums.PaymentMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrderPdfDocumentServiceTest {

    private OrderPdfDocumentService pdfService;
    private Order order;

    @BeforeEach
    void setUp() {
        pdfService = new OrderPdfDocumentService();
        order = new Order();
        order.setOrderId(123L);
        order.setUserId(1L);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setPaymentMode(PaymentMode.CARD);
        order.setStatus(OrderStatus.CONFIRMED);
        
        OrderItem item = OrderItem.builder()
                .title("Test Book")
                .quantity(1)
                .price(new BigDecimal("1000.00"))
                .lineTotal(new BigDecimal("1000.00"))
                .build();
        order.setItems(List.of(item));
    }

    @Test
    void testBuildInvoicePdf() throws Exception {
        Map<String, Object> address = Map.of(
            "line1", "Line 1",
            "city", "City",
            "state", "State",
            "postalCode", "123456",
            "country", "Country"
        );
        byte[] pdf = pdfService.buildInvoicePdf(order, address);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testBuildPaymentReceiptPdf() throws Exception {
        byte[] pdf = pdfService.buildPaymentReceiptPdf(order);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testBuildInteractiveHtml() {
        String html = pdfService.buildInteractiveHtml(order, "test@ex.com");
        assertNotNull(html);
        assertTrue(html.contains("Test Book"));
    }

    @Test
    void testBuildStatusUpdateHtml() {
        String html = pdfService.buildStatusUpdateHtml(order, "test@ex.com", OrderStatus.SHIPPED);
        assertNotNull(html);
        assertTrue(html.contains("Shipped"));
        
        html = pdfService.buildStatusUpdateHtml(order, "test@ex.com", OrderStatus.DELIVERED);
        assertTrue(html.contains("delivered"));
        
        html = pdfService.buildStatusUpdateHtml(order, "test@ex.com", OrderStatus.CANCELLED);
        assertTrue(html.contains("cancelled"));

        html = pdfService.buildStatusUpdateHtml(order, "test@ex.com", OrderStatus.FAILED);
        assertTrue(html.contains("failed"));
    }
}
