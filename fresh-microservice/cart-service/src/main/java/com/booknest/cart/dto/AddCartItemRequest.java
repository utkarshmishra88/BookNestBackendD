package com.booknest.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCartItemRequest {

    @NotNull(message = "Book ID is required")
    @Min(value = 1, message = "Book ID must be positive")
    private Integer bookId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}