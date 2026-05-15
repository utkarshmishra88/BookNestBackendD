package com.booknest.order.messaging;

import com.booknest.order.dto.OrderDocumentsEmailDto;
import com.booknest.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderNotification(Order order, OrderDocumentsEmailDto emailDto) {
        OrderConfirmedEvent event = OrderConfirmedEvent.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .toEmail(emailDto.getToEmail())
                .subject(emailDto.getSubject())
                .htmlBody(emailDto.getHtmlBody())
                .invoicePdfBase64(emailDto.getInvoicePdfBase64())
                .receiptPdfBase64(emailDto.getReceiptPdfBase64())
                .mobileNumber(emailDto.getMobileNumber())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.ORDER_CONFIRMED_ROUTING_KEY,
                event
        );
    }
}
