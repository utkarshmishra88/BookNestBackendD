package com.booknest.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthInternalClient {

    @GetMapping("/auth/internal/users/{userId}/email")
    Map<String, String> getUserEmail(
            @RequestHeader("X-Internal-Api-Key") String apiKey,
            @PathVariable("userId") Integer userId);

    @GetMapping("/auth/internal/addresses/{addressId}")
    Map<String, Object> getAddress(
            @RequestHeader("X-Internal-Api-Key") String apiKey,
            @PathVariable("addressId") Integer addressId);
}
