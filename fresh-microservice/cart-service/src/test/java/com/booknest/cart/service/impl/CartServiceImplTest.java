package com.booknest.cart.service.impl;

import com.booknest.cart.client.BookClient;
import com.booknest.cart.entity.Cart;
import com.booknest.cart.entity.CartItem;
import com.booknest.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookClient bookClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private CartItem cartItem;
    private BookClient.BookDto bookDto;

    @BeforeEach
    void setUp() {
        cartItem = CartItem.builder()
                .cartItemId(1)
                .bookId(10)
                .quantity(1)
                .price(20.0)
                .build();

        cart = Cart.builder()
                .cartId(1)
                .userId(1)
                .totalPrice(20.0)
                .cartItems(new ArrayList<>(java.util.List.of(cartItem)))
                .build();
        cartItem.setCart(cart);

        bookDto = new BookClient.BookDto(10, "Test Book", "Test Author", "http://image.url", 20.0, 50);
    }

    @Test
    void testGetCartByUserId_Exists() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        Cart result = cartService.getCartByUserId(1);

        assertEquals(1, result.getCartId());
        assertEquals(20.0, result.getTotalPrice());
    }

    @Test
    void testGetCartByUserId_CreatesNew() {
        when(cartRepository.findByUserId(2)).thenReturn(Optional.empty());
        Cart newCart = Cart.builder().userId(2).totalPrice(0.0).cartItems(new ArrayList<>()).build();
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        Cart result = cartService.getCartByUserId(2);

        assertEquals(2, result.getUserId());
        assertEquals(0.0, result.getTotalPrice());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_NewItem_Success() {
        cart.getCartItems().clear(); // empty cart
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(bookClient.getBookById(10)).thenReturn(bookDto);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.addItemToCart(1, 10, 2);

        assertEquals(1, result.getCartItems().size());
        assertEquals(40.0, result.getTotalPrice());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItemToCart_ExistingItem_Success() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(bookClient.getBookById(10)).thenReturn(bookDto);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.addItemToCart(1, 10, 2);

        assertEquals(1, result.getCartItems().size());
        assertEquals(3, result.getCartItems().get(0).getQuantity());
        assertEquals(60.0, result.getTotalPrice());
    }

    @Test
    void testAddItemToCart_InsufficientStock() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        bookDto = new BookClient.BookDto(10, "Test Book", "Test Author", "http://image.url", 20.0, 2); // only 2 in stock
        when(bookClient.getBookById(10)).thenReturn(bookDto);

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.addItemToCart(1, 10, 2)); // already has 1, needs 3, so insufficient
        assertEquals("Cannot add. Only 2 in stock.", exception.getMessage());
    }

    @Test
    void testAddItemToCart_BookNotFound() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(bookClient.getBookById(10)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> cartService.addItemToCart(1, 10, 1));
        assertEquals("Book not found.", exception.getMessage());
    }

    @Test
    void testRemoveItemFromCart() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.removeItemFromCart(1, 10);

        assertTrue(result.getCartItems().isEmpty());
        assertEquals(0.0, result.getTotalPrice());
    }

    @Test
    void testClearCart() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.clearCart(1);

        assertTrue(result.getCartItems().isEmpty());
        assertEquals(0.0, result.getTotalPrice());
    }
}
