package com.booknest.auth.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOtpRequested(String email, String otp, String mobileNumber) {
        AuthOtpRequestedEvent event = AuthOtpRequestedEvent.builder()
                .email(email)
                .otp(otp)
                .mobileNumber(mobileNumber)
                .build();
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.AUTH_OTP_ROUTING_KEY,
                event
        );
    }

    public void publishUpdateRequested(String email, String subject, String message, String mobileNumber) {
        AuthUpdateRequestedEvent event = AuthUpdateRequestedEvent.builder()
                .email(email)
                .subject(subject)
                .message(message)
                .mobileNumber(mobileNumber)
                .build();
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE,
                RabbitMqConfig.AUTH_UPDATE_ROUTING_KEY,
                event
        );
    }
}
