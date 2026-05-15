package com.booknest.wishlist.resource;

import com.booknest.wishlist.entity.WishlistItem;
import com.booknest.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlists")
@RequiredArgsConstructor
public class WishlistResource {

    private final WishlistService wishlistService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<WishlistItem>> getWishlist(@PathVariable int userId) {
        return new ResponseEntity<>(wishlistService.getUserWishlist(userId), HttpStatus.OK);
    }

    @PostMapping("/{userId}/add/{bookId}")
    public ResponseEntity<WishlistItem> addToWishlist(@PathVariable int userId, @PathVariable int bookId) {
        return new ResponseEntity<>(wishlistService.addToWishlist(userId, bookId), HttpStatus.CREATED);
    }

    @DeleteMapping("/{userId}/remove/{bookId}")
    public ResponseEntity<String> removeFromWishlist(@PathVariable int userId, @PathVariable int bookId) {
        wishlistService.removeFromWishlist(userId, bookId);
        return new ResponseEntity<>("Removed from wishlist", HttpStatus.OK);
    }

    @PostMapping("/{userId}/move-to-cart/{bookId}")
    public ResponseEntity<String> moveItemToCart(
            @PathVariable int userId, 
            @PathVariable int bookId,
            @RequestHeader("Authorization") String token) {
        
        wishlistService.moveItemToCart(userId, bookId, token);
        return new ResponseEntity<>("Item successfully moved to cart", HttpStatus.OK);
    }
}