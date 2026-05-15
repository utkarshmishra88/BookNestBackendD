package com.booknest.wallet.service.impl;

import com.booknest.wallet.dto.*;
import com.booknest.wallet.entity.Statement;
import com.booknest.wallet.entity.Wallet;
import com.booknest.wallet.repository.StatementRepository;
import com.booknest.wallet.repository.WalletRepository;
import com.booknest.wallet.service.PaymentClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = Wallet.builder()
                .walletId(1L)
                .userId(1L)
                .balance(BigDecimal.valueOf(100.0))
                .build();
    }

    @Test
    void testGetWallet_Exists() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(statementRepository.findByWalletWalletIdOrderByTransactionDateDesc(1L)).thenReturn(List.of());

        WalletResponse response = walletService.getWallet(1L);

        assertEquals(1L, response.getUserId());
        assertEquals(BigDecimal.valueOf(100.0), response.getBalance());
    }

    @Test
    void testGetWallet_CreatesNew() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        Wallet newWallet = Wallet.builder().walletId(2L).userId(1L).balance(BigDecimal.ZERO).build();
        when(walletRepository.save(any(Wallet.class))).thenReturn(newWallet);
        when(statementRepository.findByWalletWalletIdOrderByTransactionDateDesc(2L)).thenReturn(List.of());

        WalletResponse response = walletService.getWallet(1L);

        assertEquals(1L, response.getUserId());
        assertEquals(BigDecimal.ZERO, response.getBalance());
    }

    @Test
    void testCreateTopUpOrder() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        Map<String, Object> paymentResp = new HashMap<>();
        paymentResp.put("paymentId", 123L);
        paymentResp.put("status", "CREATED");
        when(paymentClient.createPaymentOrder(anyMap())).thenReturn(paymentResp);

        CreateRazorpayOrderRequest req = new CreateRazorpayOrderRequest();
        req.setAmount(BigDecimal.valueOf(50.0));

        CreateRazorpayOrderResponse response = walletService.createTopUpOrder(1L, req);

        assertEquals(123L, response.getPaymentId());
        assertEquals("CREATED", response.getStatus());
    }

    @Test
    void testCreateTopUpOrder_InvalidAmount() {
        CreateRazorpayOrderRequest req = new CreateRazorpayOrderRequest();
        req.setAmount(BigDecimal.valueOf(0.5));

        assertThrows(IllegalArgumentException.class, () -> walletService.createTopUpOrder(1L, req));
    }

    @Test
    void testVerifyTopUp_Success() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        Map<String, Object> verifyResp = new HashMap<>();
        verifyResp.put("status", "SUCCESS");
        when(paymentClient.verifyPayment(anyMap())).thenReturn(verifyResp);

        when(statementRepository.existsByWalletWalletIdAndRemarks(eq(1L), anyString())).thenReturn(false);

        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("userId", 1L);
        paymentDetails.put("amount", 50.0);
        when(paymentClient.getPaymentById(anyLong())).thenReturn(paymentDetails);

        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(statementRepository.save(any(Statement.class))).thenReturn(new Statement());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(statementRepository.findByWalletWalletIdOrderByTransactionDateDesc(1L)).thenReturn(List.of());

        VerifyTopUpRequest req = new VerifyTopUpRequest();
        req.setPaymentId(123L);
        WalletResponse response = walletService.verifyTopUp(1L, req);

        assertEquals(BigDecimal.valueOf(150.0), response.getBalance());
        verify(walletRepository, times(1)).save(wallet);
        verify(statementRepository, times(1)).save(any(Statement.class));
    }

    @Test
    void testDebit_Success() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(statementRepository.save(any(Statement.class))).thenReturn(new Statement());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(statementRepository.findByWalletWalletIdOrderByTransactionDateDesc(1L)).thenReturn(List.of());

        DebitRequest req = new DebitRequest();
        req.setAmount(BigDecimal.valueOf(50.0));

        WalletResponse response = walletService.debit(1L, req);

        assertEquals(BigDecimal.valueOf(50.0), response.getBalance());
        verify(walletRepository, times(1)).save(wallet);
        verify(statementRepository, times(1)).save(any(Statement.class));
    }

    @Test
    void testDebit_InsufficientBalance() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        DebitRequest req = new DebitRequest();
        req.setAmount(BigDecimal.valueOf(150.0));

        assertThrows(IllegalArgumentException.class, () -> walletService.debit(1L, req));
    }

    @Test
    void testVerifyTopUp_PaymentFailed() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        Map<String, Object> verifyResp = new HashMap<>();
        verifyResp.put("status", "FAILED");
        when(paymentClient.verifyPayment(anyMap())).thenReturn(verifyResp);

        VerifyTopUpRequest req = new VerifyTopUpRequest();
        req.setPaymentId(123L);

        assertThrows(IllegalArgumentException.class, () -> walletService.verifyTopUp(1L, req));
    }

    @Test
    void testVerifyTopUp_AlreadyCredited() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(statementRepository.existsByWalletWalletIdAndRemarks(eq(1L), anyString())).thenReturn(true);
        when(statementRepository.findByWalletWalletIdOrderByTransactionDateDesc(1L)).thenReturn(List.of());

        VerifyTopUpRequest req = new VerifyTopUpRequest();
        req.setPaymentId(123L);

        Map<String, Object> verifyResp = new HashMap<>();
        verifyResp.put("status", "SUCCESS");
        when(paymentClient.verifyPayment(anyMap())).thenReturn(verifyResp);

        WalletResponse response = walletService.verifyTopUp(1L, req);

        assertEquals(BigDecimal.valueOf(100.0), response.getBalance());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void testVerifyTopUp_SecurityException() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        Map<String, Object> verifyResp = new HashMap<>();
        verifyResp.put("status", "SUCCESS");
        when(paymentClient.verifyPayment(anyMap())).thenReturn(verifyResp);

        when(statementRepository.existsByWalletWalletIdAndRemarks(eq(1L), anyString())).thenReturn(false);

        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("userId", 2L); // Different user
        when(paymentClient.getPaymentById(anyLong())).thenReturn(paymentDetails);

        VerifyTopUpRequest req = new VerifyTopUpRequest();
        req.setPaymentId(123L);

        assertThrows(SecurityException.class, () -> walletService.verifyTopUp(1L, req));
    }

    @Test
    void testVerifyTopUp_InvalidAmount() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        Map<String, Object> verifyResp = new HashMap<>();
        verifyResp.put("status", "SUCCESS");
        when(paymentClient.verifyPayment(anyMap())).thenReturn(verifyResp);

        when(statementRepository.existsByWalletWalletIdAndRemarks(eq(1L), anyString())).thenReturn(false);

        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("userId", 1L);
        paymentDetails.put("amount", -10.0);
        when(paymentClient.getPaymentById(anyLong())).thenReturn(paymentDetails);

        VerifyTopUpRequest req = new VerifyTopUpRequest();
        req.setPaymentId(123L);

        assertThrows(IllegalArgumentException.class, () -> walletService.verifyTopUp(1L, req));
    }

    @Test
    void testDebit_InvalidAmount() {
        DebitRequest req = new DebitRequest();
        req.setAmount(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> walletService.debit(1L, req));
    }

    @Test
    void testDebit_CustomRemarks() {
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(statementRepository.save(any(Statement.class))).thenReturn(new Statement());
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(statementRepository.findByWalletWalletIdOrderByTransactionDateDesc(1L)).thenReturn(List.of());

        DebitRequest req = new DebitRequest();
        req.setAmount(BigDecimal.valueOf(10.0));
        req.setRemarks("Custom Remark");

        walletService.debit(1L, req);

        verify(statementRepository).save(argThat(s -> "Custom Remark".equals(s.getRemarks())));
    }
}

