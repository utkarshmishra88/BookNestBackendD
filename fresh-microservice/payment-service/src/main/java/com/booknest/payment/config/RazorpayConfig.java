package com.booknest.payment.config;

import com.razorpay.RazorpayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Bean
    public RazorpayClient razorpayClient(
            @Value("${razorpay.key-id:}") String keyId,
            @Value("${razorpay.key-secret:}") String keySecret
    ) throws Exception {
        String kid = keyId != null ? keyId.trim() : "";
        String sec = keySecret != null ? keySecret.trim() : "";
        if (kid.isEmpty() || sec.isEmpty()) {
            throw new IllegalStateException(
                    "Razorpay keys are not configured. Set environment variables RAZORPAY_KEY_ID and "
                            + "RAZORPAY_KEY_SECRET (Test keys from https://dashboard.razorpay.com/app/keys ), "
                            + "or define razorpay.key-id / razorpay.key-secret. See payment-service/env.example");
        }
        return new RazorpayClient(kid, sec);
    }
}
