package com.booknest.wishlist.resource;

import com.booknest.wishlist.entity.WishlistItem;
import com.booknest.wishlist.service.WishlistService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class WishlistResourceTest {

    @Mock
    private WishlistService wishlistService;

    @InjectMocks
    private WishlistResource wishlistResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getWishlist_ShouldReturnList() {
        when(wishlistService.getUserWishlist(1)).thenReturn(Collections.singletonList(new WishlistItem()));
        ResponseEntity<List<WishlistItem>> result = wishlistResource.getWishlist(1);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void addToWishlist_ShouldReturnItem() {
        WishlistItem item = new WishlistItem();
        when(wishlistService.addToWishlist(1, 1)).thenReturn(item);
        ResponseEntity<WishlistItem> result = wishlistResource.addToWishlist(1, 1);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(item, result.getBody());
    }

    @Test
    void removeFromWishlist_ShouldReturnOk() {
        doNothing().when(wishlistService).removeFromWishlist(anyInt(), anyInt());
        ResponseEntity<String> result = wishlistResource.removeFromWishlist(1, 1);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Removed from wishlist", result.getBody());
    }

    @Test
    void moveItemToCart_ShouldReturnOk() {
        doNothing().when(wishlistService).moveItemToCart(anyInt(), anyInt(), anyString());
        ResponseEntity<String> result = wishlistResource.moveItemToCart(1, 1, "Bearer token");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Item successfully moved to cart", result.getBody());
    }
}
