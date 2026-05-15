package com.booknest.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "wallet-service")
public interface WalletServiceClient {

    @GetMapping("/wallets/{userId}")
    Map<String, Object> getWallet(@PathVariable("userId") Long userId);

    @PostMapping("/wallets/{userId}/debit")
    Map<String, Object> debit(@PathVariable("userId") Long userId, @RequestBody Map<String, Object> request);
}

