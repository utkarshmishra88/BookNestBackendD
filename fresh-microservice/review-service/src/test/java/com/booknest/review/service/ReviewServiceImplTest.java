package com.booknest.review.service;

import com.booknest.review.client.OrderClient;
import com.booknest.review.entity.Review;
import com.booknest.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderClient orderClient;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private OrderClient.OrderDto orderDto;
    private OrderClient.OrderItemDto itemDto;

    @BeforeEach
    void setUp() {
        review = Review.builder()
                .reviewId(1)
                .userId(100)
                .bookId(10)
                .rating(4.5)
                .comment("Great book!")
                .build();

        itemDto = new OrderClient.OrderItemDto(10);
        orderDto = new OrderClient.OrderDto(1, "CONFIRMED", List.of(itemDto));
    }

    @Test
    void testAddReview_Success() {
        when(reviewRepository.findByUserIdAndBookId(100, 10)).thenReturn(Optional.empty());
        when(orderClient.getUserOrders(100, "Bearer token")).thenReturn(List.of(orderDto));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.addReview(100, 10, 4.5, "Great book!", "Bearer token");

        assertEquals(1, result.getReviewId());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testAddReview_InvalidRating() {
        assertThrows(IllegalArgumentException.class, () -> reviewService.addReview(100, 10, 6.0, "Great book!", "token"));
        assertThrows(IllegalArgumentException.class, () -> reviewService.addReview(100, 10, 0.0, "Great book!", "token"));
    }

    @Test
    void testAddReview_AlreadyReviewed() {
        when(reviewRepository.findByUserIdAndBookId(100, 10)).thenReturn(Optional.of(review));

        Exception exception = assertThrows(RuntimeException.class, () -> reviewService.addReview(100, 10, 4.5, "Great book!", "token"));
        assertEquals("You have already reviewed this book.", exception.getMessage());
    }

    @Test
    void testAddReview_NotPurchased() {
        when(reviewRepository.findByUserIdAndBookId(100, 10)).thenReturn(Optional.empty());
        
        // Order exists but for a different book
        OrderClient.OrderItemDto otherItemDto = new OrderClient.OrderItemDto(99);
        OrderClient.OrderDto otherOrderDto = new OrderClient.OrderDto(2, "CONFIRMED", List.of(otherItemDto));

        when(orderClient.getUserOrders(100, "token")).thenReturn(List.of(otherOrderDto));

        Exception exception = assertThrows(RuntimeException.class, () -> reviewService.addReview(100, 10, 4.5, "Great book!", "token"));
        assertEquals("Verified Purchase Required: You can only review books you have successfully ordered.", exception.getMessage());
    }

    @Test
    void testAddReview_OrderCancelled() {
        when(reviewRepository.findByUserIdAndBookId(100, 10)).thenReturn(Optional.empty());
        
        // Order exists for this book but it is cancelled
        OrderClient.OrderDto cancelledOrderDto = new OrderClient.OrderDto(3, "FAILED", List.of(itemDto));

        when(orderClient.getUserOrders(100, "token")).thenReturn(List.of(cancelledOrderDto));

        Exception exception = assertThrows(RuntimeException.class, () -> reviewService.addReview(100, 10, 4.5, "Great book!", "token"));
        assertEquals("Verified Purchase Required: You can only review books you have successfully ordered.", exception.getMessage());
    }

    @Test
    void testGetReviewsForBook() {
        when(reviewRepository.findByBookId(10)).thenReturn(List.of(review));
        List<Review> list = reviewService.getReviewsForBook(10);
        assertEquals(1, list.size());
    }

    @Test
    void testDeleteReview() {
        reviewService.deleteReview(1);
        verify(reviewRepository, times(1)).deleteById(1);
    }
}
