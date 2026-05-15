package com.booknest.book.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer bookId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "author", nullable = false, length = 150)
    private String author;

    @Column(name = "isbn", unique = true, length = 20)
    private String isbn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "publisher", length = 150)
    private String publisher;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "published_date")
    private LocalDate publishedDate;
}