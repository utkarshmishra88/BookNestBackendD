package com.booknest.review.repository;

import com.booknest.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByBookId(Integer bookId);
    Optional<Review> findByUserIdAndBookId(Integer userId, Integer bookId);
}