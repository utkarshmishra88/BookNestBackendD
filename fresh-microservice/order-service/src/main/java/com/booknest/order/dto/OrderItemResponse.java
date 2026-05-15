package com.booknest.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {

    // No-arg constructor for builder
    public OrderItemResponse() {}
    private Long orderItemId;
    private Long bookId;
    private String title;
    private String author;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal lineTotal;

    // Explicit builder factory method (Lombok @Builder backup)
    public static OrderItemResponseBuilder builder() {
        return new OrderItemResponseBuilder();
    }

    public static class OrderItemResponseBuilder {
        private Long orderItemId;
        private Long bookId;
        private String title;
        private String author;
        private String imageUrl;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal lineTotal;

        public OrderItemResponseBuilder orderItemId(Long orderItemId) { this.orderItemId = orderItemId; return this; }
        public OrderItemResponseBuilder bookId(Long bookId) { this.bookId = bookId; return this; }
        public OrderItemResponseBuilder title(String title) { this.title = title; return this; }
        public OrderItemResponseBuilder author(String author) { this.author = author; return this; }
        public OrderItemResponseBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public OrderItemResponseBuilder price(BigDecimal price) { this.price = price; return this; }
        public OrderItemResponseBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderItemResponseBuilder lineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; return this; }

        public OrderItemResponse build() {
            OrderItemResponse response = new OrderItemResponse();
            response.orderItemId = this.orderItemId;
            response.bookId = this.bookId;
            response.title = this.title;
            response.author = this.author;
            response.imageUrl = this.imageUrl;
            response.price = this.price;
            response.quantity = this.quantity;
            response.lineTotal = this.lineTotal;
            return response;
        }
    }
}