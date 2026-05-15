package com.booknest.review.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(name = "rating", nullable = false)
    private Double rating; // Example: 4.5

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "review_date", updatable = false)
    private LocalDateTime reviewDate;

    @PrePersist
    protected void onCreate() {
        this.reviewDate = LocalDateTime.now();
    }
}