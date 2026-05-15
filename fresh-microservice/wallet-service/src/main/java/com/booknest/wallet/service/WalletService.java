package com.booknest.wallet.service;

import com.booknest.wallet.dto.*;

public interface WalletService {

    WalletResponse getWallet(Long userId);

    CreateRazorpayOrderResponse createTopUpOrder(Long userId, CreateRazorpayOrderRequest request);

    WalletResponse verifyTopUp(Long userId, VerifyTopUpRequest request);

    WalletResponse debit(Long userId, DebitRequest request);
}