package com.booknest.review.service;

import com.booknest.review.client.OrderClient;
import com.booknest.review.entity.Review;
import com.booknest.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderClient orderClient; // Feign Client

    @Override
    public Review addReview(Integer userId, Integer bookId, Double rating, String comment, String token) {
        
        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }

        // 1. Prevent multiple reviews from the same user for the same book
        if (reviewRepository.findByUserIdAndBookId(userId, bookId).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,
                "You have already reviewed this book."
            );
        }

        // 2. Fetch all orders for this user via Feign Client
        List<OrderClient.OrderDto> userOrders = orderClient.getUserOrders(userId, token);

        // 3. Verify they actually purchased the book successfully
        boolean hasPurchased = userOrders.stream()
            // Ensure the order wasn't cancelled or failed
            .filter(order -> "CONFIRMED".equals(order.status()) || "SHIPPED".equals(order.status()) || "DELIVERED".equals(order.status()))
            // Check if the specific book is inside the items of any of these orders
            .anyMatch(order -> order.items() != null && order.items().stream()
                .anyMatch(item -> item.bookId().equals(bookId))
            );

        if (!hasPurchased) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Verified Purchase Required: You can only review books you have successfully ordered."
            );
        }

        // 4. Save Review
        Review review = Review.builder()
                .userId(userId)
                .bookId(bookId)
                .rating(rating)
                .comment(comment)
                .build();

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewsForBook(Integer bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Override
    public void deleteReview(Integer reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}