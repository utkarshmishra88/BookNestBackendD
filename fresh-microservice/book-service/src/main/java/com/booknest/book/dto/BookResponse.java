package com.booknest.book.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
    private Integer bookId;
    private String title;
    private String author;
    private String isbn;
    private CategoryResponse category;
    private String publisher;
    private Double price;
    private Integer stock;
    private Double rating;
    private String description;
    private String coverImageUrl;
    private LocalDate publishedDate;
}