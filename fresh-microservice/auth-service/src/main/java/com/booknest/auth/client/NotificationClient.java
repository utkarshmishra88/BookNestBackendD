package com.booknest.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.http.MediaType;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {

    @PostMapping(value = "/notifications/send-otp", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void sendOtp(@RequestParam("email") String email, @RequestParam("otp") String otp, @RequestParam(value = "mobileNumber", required = false) String mobileNumber);

    @PostMapping(value = "/notifications/send-update", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void sendUpdate(@RequestParam("email") String email, @RequestParam("subject") String subject, @RequestParam("message") String message, @RequestParam(value = "mobileNumber", required = false) String mobileNumber);
}