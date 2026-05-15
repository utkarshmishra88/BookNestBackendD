package com.booknest.review.dto;

import lombok.Data;

@Data
public class AddReviewRequest {
    private Integer bookId;
    private Double rating;
    private String comment;
}
