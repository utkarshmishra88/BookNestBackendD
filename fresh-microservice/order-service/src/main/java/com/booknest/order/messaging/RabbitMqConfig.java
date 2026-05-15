package com.booknest.order.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "booknest.events";
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";

    @Bean
    public TopicExchange booknestEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
