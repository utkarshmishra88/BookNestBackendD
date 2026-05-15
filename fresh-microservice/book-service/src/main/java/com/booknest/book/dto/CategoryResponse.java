package com.booknest.book.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Integer categoryId;
    private String categoryName;
    private String description;
}