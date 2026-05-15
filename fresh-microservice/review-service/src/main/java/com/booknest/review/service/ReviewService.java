package com.booknest.review.service;

import com.booknest.review.entity.Review;
import java.util.List;

public interface ReviewService {
    Review addReview(Integer userId, Integer bookId, Double rating, String comment, String token);
    List<Review> getReviewsForBook(Integer bookId);
    void deleteReview(Integer reviewId);
}