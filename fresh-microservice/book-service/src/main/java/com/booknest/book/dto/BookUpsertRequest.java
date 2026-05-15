package com.booknest.book.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookUpsertRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 150)
    private String author;

    @Size(max = 20)
    private String isbn;

    @NotNull(message = "Category ID is required")
    @Min(value = 1, message = "Category ID must be positive")
    private Integer categoryId;

    @Size(max = 150)
    private String publisher;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private Double price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5")
    private Double rating;

    private String description;

    @Size(max = 500)
    private String coverImageUrl;

    private LocalDate publishedDate;
}