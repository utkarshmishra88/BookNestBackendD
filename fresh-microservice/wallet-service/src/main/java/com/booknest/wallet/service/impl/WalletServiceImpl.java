package com.booknest.wallet.service.impl;

import com.booknest.wallet.dto.*;
import com.booknest.wallet.entity.Statement;
import com.booknest.wallet.entity.Wallet;
import com.booknest.wallet.repository.StatementRepository;
import com.booknest.wallet.repository.WalletRepository;
import com.booknest.wallet.service.PaymentClient;
import com.booknest.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final StatementRepository statementRepository;
    private final PaymentClient paymentClient;

    public WalletServiceImpl(WalletRepository walletRepository,
                             StatementRepository statementRepository,
                             PaymentClient paymentClient) {
        this.walletRepository = walletRepository;
        this.statementRepository = statementRepository;
        this.paymentClient = paymentClient;
    }

    @Override
    public WalletResponse getWallet(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return toWalletResponse(wallet);
    }

    @Override
    public CreateRazorpayOrderResponse createTopUpOrder(Long userId, CreateRazorpayOrderRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Topup amount must be at least 1");
        }

        getOrCreateWallet(userId);

        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderId", System.currentTimeMillis());
        paymentRequest.put("userId", userId);
        paymentRequest.put("amount", request.getAmount());
        // Online top-up via Razorpay (Card / UPI / NB / etc.) — not in-app wallet debit.
        paymentRequest.put("paymentMode", "UPI");

        Map<String, Object> paymentResp = paymentClient.createPaymentOrder(paymentRequest);

        return CreateRazorpayOrderResponse.builder()
            .paymentId(asLong(paymentResp.get("paymentId"), null))
            .razorpayOrderId(asString(paymentResp.get("razorpayOrderId"), null))
            .razorpayKeyId(asString(paymentResp.get("razorpayKeyId"), null))
            .status(asString(paymentResp.get("status"), "CREATED"))
            .message(asString(paymentResp.get("message"), "Payment order created"))
            .build();
    }

    @Override
    @Transactional
    public WalletResponse verifyTopUp(Long userId, VerifyTopUpRequest request) {
        Wallet wallet = getOrCreateWallet(userId);

        Map<String, Object> verifyReq = new HashMap<>();
        verifyReq.put("paymentId", request.getPaymentId());
        verifyReq.put("razorpayOrderId", request.getRazorpayOrderId());
        verifyReq.put("razorpayPaymentId", request.getRazorpayPaymentId());
        verifyReq.put("razorpaySignature", request.getRazorpaySignature());

        Map<String, Object> verifyResp = paymentClient.verifyPayment(verifyReq);
        String status = asString(verifyResp.get("status"), "FAILED");

        if (!"SUCCESS".equalsIgnoreCase(status) && !"PAID".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Payment verification failed");
        }

        String idempotencyRemark = "TOPUP_PAYMENT_ID:" + request.getPaymentId();
        boolean alreadyCredited = statementRepository.existsByWalletWalletIdAndRemarks(wallet.getWalletId(), idempotencyRemark);
        if (alreadyCredited) {
            return toWalletResponse(wallet);
        }

        Map<String, Object> paymentDetails = paymentClient.getPaymentById(request.getPaymentId());
        Long paymentUserId = asLong(paymentDetails.get("userId"), null);
        if (paymentUserId == null || !paymentUserId.equals(userId)) {
            throw new SecurityException("Payment does not belong to this user");
        }

        BigDecimal amount = asBigDecimal(paymentDetails.get("amount"), BigDecimal.ZERO);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Statement st = Statement.builder()
            .wallet(wallet)
            .transactionType(transactionTypeCredit())
            .amount(amount)
            .remarks(idempotencyRemark)
            .transactionDate(LocalDateTime.now())
            .build();
        statementRepository.save(st);

        return toWalletResponse(walletRepository.findById(wallet.getWalletId()).orElse(wallet));
    }

    @Override
    @Transactional
    public WalletResponse debit(Long userId, DebitRequest request) {
        Wallet wallet = getOrCreateWallet(userId);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be greater than 0");
        }

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        String remarks = request.getRemarks() == null || request.getRemarks().isBlank()
            ? "WALLET_DEBIT"
            : request.getRemarks();

        Statement st = Statement.builder()
            .wallet(wallet)
            .transactionType(transactionTypeDebit())
            .amount(request.getAmount())
            .remarks(remarks)
            .transactionDate(LocalDateTime.now())
            .build();
        statementRepository.save(st);

        return toWalletResponse(walletRepository.findById(wallet.getWalletId()).orElse(wallet));
    }

    private Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> walletRepository.save(
            Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .build()
        ));
    }

    private WalletResponse toWalletResponse(Wallet wallet) {
        List<StatementResponse> statements = statementRepository
            .findByWalletWalletIdOrderByTransactionDateDesc(wallet.getWalletId())
            .stream()
            .map(this::toStatementResponse)
            .collect(Collectors.toList());

        return WalletResponse.builder()
            .walletId(wallet.getWalletId())
            .userId(wallet.getUserId())
            .balance(wallet.getBalance())
            .statements(statements)
            .build();
    }

    private StatementResponse toStatementResponse(Statement st) {
        return StatementResponse.builder()
            .statementId(st.getStatementId())
            .transactionType(st.getTransactionType())
            .amount(st.getAmount())
            .remarks(st.getRemarks())
            .transactionDate(st.getTransactionDate())
            .build();
    }

    private String transactionTypeCredit() {
        return "CREDIT";
    }

    private String transactionTypeDebit() {
        return "DEBIT";
    }

    private String asString(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private Long asLong(Object value, Long fallback) {
        if (value == null) return fallback;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof Long l) return l;
        if (value instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }

    private BigDecimal asBigDecimal(Object value, BigDecimal fallback) {
        if (value == null) return fallback;
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }
}