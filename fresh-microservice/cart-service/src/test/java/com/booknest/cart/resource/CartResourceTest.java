package com.booknest.cart.resource;

import com.booknest.cart.dto.AddCartItemRequest;
import com.booknest.cart.entity.Cart;
import com.booknest.cart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CartResourceTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartResource cartResource;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("1");
    }

    @Test
    void getCart_ShouldReturnCart() {
        Cart cart = new Cart();
        when(cartService.getCartByUserId(1)).thenReturn(cart);

        ResponseEntity<Cart> result = cartResource.getCart(1, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(cart, result.getBody());
    }

    @Test
    void addItem_ShouldReturnCart() {
        Cart cart = new Cart();
        AddCartItemRequest request = new AddCartItemRequest();
        request.setBookId(10);
        request.setQuantity(2);
        when(cartService.addItemToCart(1, 10, 2)).thenReturn(cart);

        ResponseEntity<Cart> result = cartResource.addItem(1, request, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(cart, result.getBody());
    }

    @Test
    void removeItem_ShouldReturnCart() {
        Cart cart = new Cart();
        when(cartService.removeItemFromCart(1, 10)).thenReturn(cart);

        ResponseEntity<Cart> result = cartResource.removeItem(1, 10, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(cart, result.getBody());
    }

    @Test
    void clearCart_ShouldReturnCart() {
        Cart cart = new Cart();
        when(cartService.clearCart(1)).thenReturn(cart);

        ResponseEntity<Cart> result = cartResource.clearCart(1, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(cart, result.getBody());
    }

    @Test
    void validateOwnership_ShouldThrowUnauthorized_WhenAuthenticationNull() {
        assertThrows(RuntimeException.class, () -> cartResource.getCart(1, null));
    }

    @Test
    void validateOwnership_ShouldThrowForbidden_WhenUserIdMismatch() {
        when(authentication.getPrincipal()).thenReturn("2");
        assertThrows(RuntimeException.class, () -> cartResource.getCart(1, authentication));
    }
}
