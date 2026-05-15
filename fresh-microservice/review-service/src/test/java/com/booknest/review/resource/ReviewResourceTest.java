package com.booknest.review.resource;

import com.booknest.review.dto.AddReviewRequest;
import com.booknest.review.entity.Review;
import com.booknest.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class ReviewResourceTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewResource reviewResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getBookReviews_ShouldReturnList() {
        when(reviewService.getReviewsForBook(1)).thenReturn(Collections.singletonList(new Review()));
        ResponseEntity<List<Review>> result = reviewResource.getBookReviews(1);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void addReview_ShouldReturnCreated() {
        AddReviewRequest request = new AddReviewRequest();
        request.setBookId(1);
        request.setRating(5.0);
        request.setComment("Great!");
        
        Review response = new Review();
        when(reviewService.addReview(anyInt(), anyInt(), anyDouble(), anyString(), anyString())).thenReturn(response);
        
        ResponseEntity<Review> result = reviewResource.addReview(1, request, "Bearer token");
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteReviewAsAdmin_ShouldReturnOk() {
        doNothing().when(reviewService).deleteReview(1);
        ResponseEntity<String> result = reviewResource.deleteReviewAsAdmin(1);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Review successfully deleted by Administrator.", result.getBody());
    }
}
