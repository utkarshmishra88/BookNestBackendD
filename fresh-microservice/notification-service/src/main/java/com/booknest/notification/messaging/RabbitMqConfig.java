package com.booknest.notification.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "booknest.events";
    public static final String ORDER_QUEUE = "notification.order.queue";
    public static final String AUTH_OTP_QUEUE = "notification.auth.otp.queue";
    public static final String AUTH_UPDATE_QUEUE = "notification.auth.update.queue";
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";
    public static final String AUTH_OTP_ROUTING_KEY = "auth.otp.requested";
    public static final String AUTH_UPDATE_ROUTING_KEY = "auth.update.requested";

    @Bean
    public TopicExchange booknestEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationOrderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public Queue notificationAuthOtpQueue() {
        return new Queue(AUTH_OTP_QUEUE, true);
    }

    @Bean
    public Queue notificationAuthUpdateQueue() {
        return new Queue(AUTH_UPDATE_QUEUE, true);
    }

    @Bean
    public Binding orderConfirmedBinding(Queue notificationOrderQueue, TopicExchange booknestEventsExchange) {
        return BindingBuilder.bind(notificationOrderQueue).to(booknestEventsExchange).with(ORDER_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Binding authOtpBinding(Queue notificationAuthOtpQueue, TopicExchange booknestEventsExchange) {
        return BindingBuilder.bind(notificationAuthOtpQueue).to(booknestEventsExchange).with(AUTH_OTP_ROUTING_KEY);
    }

    @Bean
    public Binding authUpdateBinding(Queue notificationAuthUpdateQueue, TopicExchange booknestEventsExchange) {
        return BindingBuilder.bind(notificationAuthUpdateQueue).to(booknestEventsExchange).with(AUTH_UPDATE_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
