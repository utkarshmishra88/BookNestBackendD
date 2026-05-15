package com.booknest.wishlist.service;

import com.booknest.wishlist.entity.WishlistItem;
import java.util.List;

public interface WishlistService {
    List<WishlistItem> getUserWishlist(Integer userId);
    WishlistItem addToWishlist(Integer userId, Integer bookId);
    void removeFromWishlist(Integer userId, Integer bookId);
    void moveItemToCart(Integer userId, Integer bookId, String token);
}