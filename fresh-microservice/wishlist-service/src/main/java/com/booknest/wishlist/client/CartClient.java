package com.booknest.wishlist.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "CART-SERVICE")
public interface CartClient {

    @PostMapping("/carts/{userId}/add")
    void addItemToCart(
            @PathVariable("userId") Integer userId,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Object> request,
            @RequestHeader("Authorization") String token
    );
}