package com.booknest.order.messaging;

import com.booknest.order.dto.OrderDocumentsEmailDto;
import com.booknest.order.entity.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderEventPublisher orderEventPublisher;

    @Test
    void testPublishOrderNotification() {
        Order order = new Order();
        order.setOrderId(1L);
        order.setUserId(1L);

        OrderDocumentsEmailDto dto = new OrderDocumentsEmailDto();
        dto.setToEmail("test@ex.com");

        orderEventPublisher.publishOrderNotification(order, dto);

        verify(rabbitTemplate).convertAndSend(eq(RabbitMqConfig.EXCHANGE), eq(RabbitMqConfig.ORDER_CONFIRMED_ROUTING_KEY), any(OrderConfirmedEvent.class));
    }
}
