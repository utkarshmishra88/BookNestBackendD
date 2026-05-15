package com.booknest.wishlist.repository;

import com.booknest.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Integer> {
    List<WishlistItem> findByUserId(int userId);
    Optional<WishlistItem> findByUserIdAndBookId(int userId, int bookId);
    void deleteByUserIdAndBookId(int userId, int bookId);
}