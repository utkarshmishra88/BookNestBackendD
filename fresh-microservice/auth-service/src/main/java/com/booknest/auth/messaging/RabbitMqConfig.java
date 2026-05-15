package com.booknest.auth.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "booknest.events";
    public static final String AUTH_OTP_ROUTING_KEY = "auth.otp.requested";
    public static final String AUTH_UPDATE_ROUTING_KEY = "auth.update.requested";

    @Bean
    public TopicExchange booknestEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
