package com.booknest.order.client;

import com.booknest.order.dto.OrderDocumentsEmailDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationDeliveryClient {

    @PostMapping("/notifications/internal/order-documents")
    void sendOrderDocuments(
            @RequestHeader("X-Internal-Api-Key") String apiKey,
            @RequestBody OrderDocumentsEmailDto body);
}
