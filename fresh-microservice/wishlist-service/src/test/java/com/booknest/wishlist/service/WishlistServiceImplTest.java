package com.booknest.wishlist.service;

import com.booknest.wishlist.client.BookClient;
import com.booknest.wishlist.client.CartClient;
import com.booknest.wishlist.entity.WishlistItem;
import com.booknest.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private BookClient bookClient;

    @Mock
    private CartClient cartClient;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private WishlistItem item;

    @BeforeEach
    void setUp() {
        item = WishlistItem.builder()
                .wishlistItemId(1)
                .userId(100)
                .bookId(10)
                .build();
    }

    @Test
    void testGetUserWishlist() {
        when(wishlistRepository.findByUserId(100)).thenReturn(List.of(item));
        List<WishlistItem> list = wishlistService.getUserWishlist(100);
        assertEquals(1, list.size());
    }

    @Test
    void testAddToWishlist_Success() {
        when(bookClient.getBookById(10)).thenReturn(new BookClient.BookDto(10, "Title", 10.0, 10));
        when(wishlistRepository.findByUserIdAndBookId(100, 10)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(WishlistItem.class))).thenReturn(item);

        WishlistItem result = wishlistService.addToWishlist(100, 10);

        assertEquals(1, result.getWishlistItemId());
        verify(wishlistRepository, times(1)).save(any(WishlistItem.class));
    }

    @Test
    void testAddToWishlist_AlreadyInWishlist() {
        when(bookClient.getBookById(10)).thenReturn(new BookClient.BookDto(10, "Title", 10.0, 10));
        when(wishlistRepository.findByUserIdAndBookId(100, 10)).thenReturn(Optional.of(item));

        Exception exception = assertThrows(RuntimeException.class, () -> wishlistService.addToWishlist(100, 10));
        assertEquals("Book is already in your wishlist.", exception.getMessage());
    }

    @Test
    void testRemoveFromWishlist() {
        wishlistService.removeFromWishlist(100, 10);
        verify(wishlistRepository, times(1)).deleteByUserIdAndBookId(100, 10);
    }

    @Test
    void testMoveItemToCart_Success() {
        when(wishlistRepository.findByUserIdAndBookId(100, 10)).thenReturn(Optional.of(item));

        wishlistService.moveItemToCart(100, 10, "Bearer token");

        verify(cartClient, times(1)).addItemToCart(eq(100), anyMap(), eq("Bearer token"));
        verify(wishlistRepository, times(1)).deleteByUserIdAndBookId(100, 10);
    }

    @Test
    void testMoveItemToCart_NotFound() {
        when(wishlistRepository.findByUserIdAndBookId(100, 10)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> wishlistService.moveItemToCart(100, 10, "token"));
        assertEquals("Item not found in wishlist.", exception.getMessage());
    }
}
