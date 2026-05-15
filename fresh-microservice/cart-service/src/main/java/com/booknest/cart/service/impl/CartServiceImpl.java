package com.booknest.cart.service.impl;

import com.booknest.cart.client.BookClient;
import com.booknest.cart.entity.Cart;
import com.booknest.cart.entity.CartItem;
import com.booknest.cart.repository.CartRepository;
import com.booknest.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final BookClient bookClient;

    @Override
    public Cart getCartByUserId(int userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .totalPrice(0.0)
                    .cartItems(new ArrayList<>())
                    .build();
            return cartRepository.save(newCart);
        });
        populateBookDetails(cart);
        return cart;
    }

    @Override
    @Transactional
    public Cart addItemToCart(int userId, int bookId, int quantity) {
        if (quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1.");
        }

        Cart cart = getCartByUserId(userId);
        BookClient.BookDto book = bookClient.getBookById(bookId);

        if (book == null) {
            throw new RuntimeException("Book not found.");
        }

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getBookId() == bookId)
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newTotalQuantity = item.getQuantity() + quantity;

            if (book.stock() < newTotalQuantity) {
                throw new RuntimeException("Cannot add. Only " + book.stock() + " in stock.");
            }

            item.setQuantity(newTotalQuantity);
            item.setPrice(book.price());
        } else {
            if (book.stock() < quantity) {
                throw new RuntimeException("Cannot add. Only " + book.stock() + " in stock.");
            }

            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .bookId(bookId)
                    .quantity(quantity)
                    .price(book.price())
                    .build();

            cart.getCartItems().add(newItem);
        }

        recalculateTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        populateBookDetails(savedCart);
        return savedCart;
    }

    @Override
    @Transactional
    public Cart removeItemFromCart(int userId, int bookId) {
        Cart cart = getCartByUserId(userId);
        cart.getCartItems().removeIf(item -> item.getBookId() == bookId);
        recalculateTotal(cart);
        Cart savedCart2 = cartRepository.save(cart);
        populateBookDetails(savedCart2);
        return savedCart2;
    }

    @Override
    @Transactional
    public Cart clearCart(int userId) {
        Cart cart = getCartByUserId(userId);
        cart.getCartItems().clear();
        cart.setTotalPrice(0.0);
        Cart savedCart = cartRepository.save(cart);
        populateBookDetails(savedCart);
        return savedCart;
    }

    private void populateBookDetails(Cart cart) {
        if (cart != null && cart.getCartItems() != null) {
            cart.getCartItems().forEach(item -> {
                try {
                    item.setBook(bookClient.getBookById(item.getBookId()));
                } catch (Exception e) {
                    // Log error or handle missing book
                }
            });
        }
    }

    private void recalculateTotal(Cart cart) {
        double total = cart.getCartItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(total);
    }
}