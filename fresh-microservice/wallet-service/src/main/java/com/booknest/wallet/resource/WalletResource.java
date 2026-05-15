package com.booknest.wallet.resource;

import com.booknest.wallet.dto.*;
import com.booknest.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
public class WalletResource {

    private final WalletService walletService;

    public WalletResource(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{userId}")
    public WalletResponse getWallet(@PathVariable Long userId, Authentication authentication) {
        authorize(userId, authentication);
        return walletService.getWallet(userId);
    }

    @PostMapping("/{userId}/topup/create-order")
    public CreateRazorpayOrderResponse createTopupOrder(@PathVariable Long userId,
                                                        @Valid @RequestBody CreateRazorpayOrderRequest request,
                                                        Authentication authentication) {
        authorize(userId, authentication);
        return walletService.createTopUpOrder(userId, request);
    }

    @PostMapping("/{userId}/topup/verify")
    public WalletResponse verifyTopup(@PathVariable Long userId,
                                      @Valid @RequestBody VerifyTopUpRequest request,
                                      Authentication authentication) {
        authorize(userId, authentication);
        return walletService.verifyTopUp(userId, request);
    }

    @PostMapping("/{userId}/debit")
    public WalletResponse debit(@PathVariable Long userId,
                                @Valid @RequestBody DebitRequest request,
                                Authentication authentication) {
        authorize(userId, authentication);
        return walletService.debit(userId, request);
    }

    private void authorize(Long pathUserId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new SecurityException("Unauthorized");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> "ROLE_ADMIN".equals(a));

        Long principalUserId;
        try {
            principalUserId = Long.parseLong(String.valueOf(authentication.getPrincipal()));
        } catch (Exception ex) {
            throw new SecurityException("Invalid user in token");
        }

        if (!isAdmin && !pathUserId.equals(principalUserId)) {
            throw new SecurityException("Forbidden: cannot access another user's wallet");
        }
    }
}