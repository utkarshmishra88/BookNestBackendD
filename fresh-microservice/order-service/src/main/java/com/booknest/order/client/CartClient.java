package com.booknest.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "cart-service")
public interface CartClient {

    @GetMapping("/carts/{userId}")
    Map<String, Object> getCartByUserId(@PathVariable("userId") Long userId);

    @DeleteMapping("/carts/{userId}/clear")
    void clearCart(@PathVariable("userId") Long userId);
}