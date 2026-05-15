package com.booknest.review.resource;

import com.booknest.review.entity.Review;
import com.booknest.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewResource {

    private final ReviewService reviewService;

    // Public API: Retrieve all reviews for a specific book
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Review>> getBookReviews(@PathVariable Integer bookId) {
        return new ResponseEntity<>(reviewService.getReviewsForBook(bookId), HttpStatus.OK);
    }

    // Protected API: Post a new review
    @PostMapping("/{userId}/add")
    public ResponseEntity<Review> addReview(
            @PathVariable Integer userId,
            @RequestBody com.booknest.review.dto.AddReviewRequest request,
            @RequestHeader("Authorization") String token) {
        
        Review newReview = reviewService.addReview(userId, request.getBookId(), request.getRating(), request.getComment(), token);
        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }

    // Admin API: Delete inappropriate reviews
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteReviewAsAdmin(@PathVariable Integer reviewId) {
        reviewService.deleteReview(reviewId);
        return new ResponseEntity<>("Review successfully deleted by Administrator.", HttpStatus.OK);
    }


}