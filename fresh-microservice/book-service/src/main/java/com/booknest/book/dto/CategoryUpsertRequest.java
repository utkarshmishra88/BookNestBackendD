package com.booknest.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryUpsertRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100)
    private String categoryName;

    @Size(max = 255)
    private String description;
}