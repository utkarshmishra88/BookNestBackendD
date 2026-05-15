package com.booknest.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;

@FeignClient(name = "ORDER-SERVICE")
public interface OrderClient {

    // Data Transfer Objects to map the response from the Order Service
    record OrderItemDto(Integer bookId) {}
    record OrderDto(Integer orderId, String status, List<OrderItemDto> items) {}

    // Feign call to fetch all orders for the user
    @GetMapping("/orders/{userId}")
    List<OrderDto> getUserOrders(@PathVariable("userId") Integer userId, @RequestHeader("Authorization") String token);
}