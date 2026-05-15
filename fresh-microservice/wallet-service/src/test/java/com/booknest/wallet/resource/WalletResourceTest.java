package com.booknest.wallet.resource;

import com.booknest.wallet.dto.CreateRazorpayOrderRequest;
import com.booknest.wallet.dto.CreateRazorpayOrderResponse;
import com.booknest.wallet.dto.DebitRequest;
import com.booknest.wallet.dto.VerifyTopUpRequest;
import com.booknest.wallet.dto.WalletResponse;
import com.booknest.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WalletResourceTest {

    private WalletResponse mockWalletResponse() {
        return WalletResponse.builder()
                .walletId(1L)
                .userId(1L)
                .balance(java.math.BigDecimal.valueOf(1000))
                .statements(Collections.emptyList())
                .build();
    }

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletResource walletResource;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("1");
        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
    }

    @Test
    void getWallet_ShouldReturnWallet() {
        WalletResponse response = mockWalletResponse();
        when(walletService.getWallet(1L)).thenReturn(response);

        WalletResponse result = walletResource.getWallet(1L, authentication);

        assertEquals(response, result);
    }

    @Test
    void createTopupOrder_ShouldReturnOrder() {
        CreateRazorpayOrderRequest request = new CreateRazorpayOrderRequest();
        CreateRazorpayOrderResponse response = CreateRazorpayOrderResponse.builder()
                .paymentId(1L)
                .razorpayOrderId("order_123")
                .razorpayKeyId("rzp_test_key")
                .status("CREATED")
                .message("created")
                .build();
        when(walletService.createTopUpOrder(eq(1L), any())).thenReturn(response);

        CreateRazorpayOrderResponse result = walletResource.createTopupOrder(1L, request, authentication);

        assertEquals(response, result);
    }

    @Test
    void verifyTopup_ShouldReturnWallet() {
        VerifyTopUpRequest request = new VerifyTopUpRequest();
        WalletResponse response = mockWalletResponse();
        when(walletService.verifyTopUp(eq(1L), any())).thenReturn(response);

        WalletResponse result = walletResource.verifyTopup(1L, request, authentication);

        assertEquals(response, result);
    }

    @Test
    void debit_ShouldReturnWallet() {
        DebitRequest request = new DebitRequest();
        WalletResponse response = mockWalletResponse();
        when(walletService.debit(eq(1L), any())).thenReturn(response);

        WalletResponse result = walletResource.debit(1L, request, authentication);

        assertEquals(response, result);
    }

    @Test
    void authorize_ShouldThrowException_WhenUserIdMismatch() {
        when(authentication.getPrincipal()).thenReturn("2");
        assertThrows(SecurityException.class, () -> walletResource.getWallet(1L, authentication));
    }

    @Test
    void getWallet_ShouldAllowAdmin_EvenIfUserIdMismatch() {
        when(authentication.getPrincipal()).thenReturn("2");
        when(authentication.getAuthorities()).thenReturn((java.util.Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        WalletResponse response = mockWalletResponse();
        when(walletService.getWallet(1L)).thenReturn(response);

        WalletResponse result = walletResource.getWallet(1L, authentication);

        assertEquals(response, result);
    }

    @Test
    void authorize_ShouldThrowException_WhenAuthenticationNull() {
        assertThrows(SecurityException.class, () -> walletResource.getWallet(1L, null));
    }

    @Test
    void authorize_ShouldThrowException_WhenPrincipalInvalid() {
        when(authentication.getPrincipal()).thenReturn("not-a-number");
        assertThrows(SecurityException.class, () -> walletResource.getWallet(1L, authentication));
    }
}

