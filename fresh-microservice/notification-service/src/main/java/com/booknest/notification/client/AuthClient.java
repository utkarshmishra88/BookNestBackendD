package com.booknest.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/auth/internal/users/all-contacts")
    List<Map<String, String>> getAllUserContacts(@RequestHeader("X-Internal-Api-Key") String key);
}
