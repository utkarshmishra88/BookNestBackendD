package com.booknest.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback")
    public Mono<Map<String, Object>> fallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 503);
        response.put("error", "Service Unavailable");
        response.put("message", "One or more services are currently down or taking too long to respond. Please try again later.");
        return Mono.just(response);
    }
}
