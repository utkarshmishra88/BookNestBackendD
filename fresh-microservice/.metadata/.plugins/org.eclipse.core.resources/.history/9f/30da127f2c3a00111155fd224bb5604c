package com.booknest.cart.resource;

import com.booknest.cart.dto.AddCartItemRequest;
import com.booknest.cart.entity.Cart;
import com.booknest.cart.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartResource {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable @Min(1) int userId, Authentication authentication) {
        validateOwnership(userId, authentication);
        return new ResponseEntity<>(cartService.getCartByUserId(userId), HttpStatus.OK);
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<Cart> addItem(
            @PathVariable @Min(1) int userId,
            @Valid @RequestBody AddCartItemRequest request,
            Authentication authentication
    ) {
        validateOwnership(userId, authentication);
        return new ResponseEntity<>(
                cartService.addItemToCart(userId, request.getBookId(), request.getQuantity()),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{userId}/remove/{bookId}")
    public ResponseEntity<Cart> removeItem(
            @PathVariable @Min(1) int userId,
            @PathVariable @Min(1) int bookId,
            Authentication authentication
    ) {
        validateOwnership(userId, authentication);
        return new ResponseEntity<>(cartService.removeItemFromCart(userId, bookId), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Cart> clearCart(@PathVariable @Min(1) int userId, Authentication authentication) {
        validateOwnership(userId, authentication);
        return new ResponseEntity<>(cartService.clearCart(userId), HttpStatus.OK);
    }

    private void validateOwnership(int pathUserId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Unauthorized");
        }

        int authUserId = Integer.parseInt(authentication.getPrincipal().toString());
        if (authUserId != pathUserId) {
            throw new RuntimeException("Forbidden: Cannot access another user's cart");
        }
    }
}