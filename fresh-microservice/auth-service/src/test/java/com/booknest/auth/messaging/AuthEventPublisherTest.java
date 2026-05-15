package com.booknest.auth.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthEventPublisher authEventPublisher;

    @Test
    void testPublishOtpRequested() {
        authEventPublisher.publishOtpRequested("test@ex.com", "123456", "1234567890");
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AuthOtpRequestedEvent.class));
    }

    @Test
    void testPublishUpdateRequested() {
        authEventPublisher.publishUpdateRequested("test@ex.com", "Sub", "Body", "123");
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(AuthUpdateRequestedEvent.class));
    }
}