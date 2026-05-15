package com.booknest.cart.service;

import com.booknest.cart.entity.Cart;

public interface CartService {
    Cart getCartByUserId(int userId);
    Cart addItemToCart(int userId, int bookId, int quantity);
    Cart removeItemFromCart(int userId, int bookId);
    Cart clearCart(int userId);
}