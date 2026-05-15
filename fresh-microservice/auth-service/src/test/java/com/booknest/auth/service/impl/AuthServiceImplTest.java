package com.booknest.auth.service.impl;

import com.booknest.auth.client.NotificationClient;
import com.booknest.auth.dto.UserResponse;
import com.booknest.auth.entity.TokenBlacklist;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.TokenBlacklistRepository;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.messaging.AuthEventPublisher;
import com.booknest.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private AuthEventPublisher authEventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .userId(1)
                .fullName("John Doe")
                .email("john@example.com")
                .passwordHash("password123")
                .role("CUSTOMER")
                .active(true)
                .mobileNumber("+919876543210")
                .build();
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        String response = authService.registerUser(validUser);

        assertEquals("Registration successful! Please check your email for the OTP.", response);
        verify(userRepository, times(1)).save(any(User.class));
        verify(authEventPublisher, times(1)).publishOtpRequested(eq("john@example.com"), anyString(), any());
    }

    @Test
    void testRegisterUser_MissingFullName() {
        validUser.setFullName("");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.registerUser(validUser));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Full name is required"));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.registerUser(validUser));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Email already exists"));
    }

    @Test
    void testVerifyOtp_Success() {
        validUser.setOtp("123456");
        validUser.setOtpRequestedAt(LocalDateTime.now());
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));
        
        String response = authService.verifyOtp("john@example.com", "123456");
        
        assertEquals("Account verified! You can now login.", response);
        assertTrue(validUser.getActive());
        assertNull(validUser.getOtp());
    }

    @Test
    void testVerifyOtp_InvalidOtp() {
        validUser.setOtp("654321");
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.verifyOtp("john@example.com", "123456"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid OTP.", exception.getReason());
    }

    @Test
    void testVerifyOtp_Expired() {
        validUser.setOtp("123456");
        validUser.setOtpRequestedAt(LocalDateTime.now().minusMinutes(6));
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.verifyOtp("john@example.com", "123456"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("expired"));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("test_token");

        String token = authService.login("john@example.com", "password123");

        assertEquals("test_token", token);
    }

    @Test
    void testLogin_UserNotActive() {
        validUser.setActive(false);
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));
        
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.login("john@example.com", "password123"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(exception.getReason().contains("verify your account via OTP"));
    }

    @Test
    void testLogout_Success() {
        when(tokenBlacklistRepository.existsByToken(anyString())).thenReturn(false);
        authService.logout("Bearer test_token");
        verify(tokenBlacklistRepository, times(1)).save(any(TokenBlacklist.class));
    }

    @Test
    void testGetProfile_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        User profile = authService.getProfile(1);
        assertEquals("John Doe", profile.getFullName());
    }

    @Test
    void testUpdateProfile_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        User updated = authService.updateProfile(1, "1", "Jane Doe", "+919999999999");

        assertEquals("Jane Doe", updated.getFullName());
        verify(userRepository).save(validUser);
    }

    @Test
    void testForgotPassword_Success() {
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(validUser));
        when(jwtUtil.generateToken(anyMap(), anyLong())).thenReturn("reset_token");

        String response = authService.forgotPassword("john@example.com");

        assertEquals("Password reset link has been sent to your email.", response);
        verify(authEventPublisher).publishUpdateRequested(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testResetPassword_Success() {
        when(jwtUtil.validateToken("reset_token")).thenReturn(true);
        when(jwtUtil.extractClaim(eq("reset_token"), any())).thenReturn("PASSWORD_RESET");
        when(jwtUtil.extractEmail("reset_token")).thenReturn("john@example.com");
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(validUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("hashedNewPassword");

        String response = authService.resetPassword("reset_token", "newPassword123");

        assertEquals("Password successfully reset! You can now log in.", response);
        assertEquals("hashedNewPassword", validUser.getPasswordHash());
        verify(tokenBlacklistRepository).save(any(TokenBlacklist.class));
    }

    @Test
    void testRequestEmailChange_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        when(userRepository.existsByEmail("new@ex.com")).thenReturn(false);

        String resp = authService.requestEmailChange(1, "1", "new@ex.com");

        assertTrue(resp.contains("Verification code sent"));
        assertEquals("new@ex.com", validUser.getPendingEmail());
        assertNotNull(validUser.getEmailChangeOtp());
    }

    @Test
    void testConfirmEmailChange_Success() {
        validUser.setPendingEmail("new@ex.com");
        validUser.setEmailChangeOtp("123456");
        validUser.setEmailChangeOtpRequestedAt(LocalDateTime.now());
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("newToken");

        String token = authService.confirmEmailChange(1, "1", "new@ex.com", "123456");

        assertEquals("newToken", token);
        assertEquals("new@ex.com", validUser.getEmail());
    }

    @Test
    void testListUsersForAdmin() {
        when(userRepository.findAll()).thenReturn(java.util.List.of(validUser));
        java.util.List<com.booknest.auth.dto.UserResponse> users = authService.listUsersForAdmin();
        assertEquals(1, users.size());
    }

    @Test
    void testSetUserActive() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        com.booknest.auth.dto.UserResponse resp = authService.setUserActive(1, false);
        assertFalse(resp.getActive());
    }

    @Test
    void testGetProfile_NotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> authService.getProfile(99));
    }

    @Test
    void testUpdateProfile_Forbidden() {
        assertThrows(ResponseStatusException.class, () -> authService.updateProfile(1, "2", "Name", "123"));
    }

    @Test
    void testRequestEmailChange_SameEmail() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        assertThrows(ResponseStatusException.class, () -> authService.requestEmailChange(1, "1", "john@example.com"));
    }

    @Test
    void testRequestEmailChange_Exists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        when(userRepository.existsByEmail("other@ex.com")).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> authService.requestEmailChange(1, "1", "other@ex.com"));
    }

    @Test
    void testConfirmEmailChange_NoPending() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        assertThrows(RuntimeException.class, () -> authService.confirmEmailChange(1, "1", "new@ex.com", "123"));
    }

    @Test
    void testForgotPassword_Inactive() {
        validUser.setActive(false);
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(validUser));
        assertThrows(ResponseStatusException.class, () -> authService.forgotPassword("john@example.com"));
    }

    @Test
    void testRequestEmailChange_EmailAlreadyTaken() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        when(userRepository.existsByEmail("taken@ex.com")).thenReturn(true);
        
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, 
            () -> authService.requestEmailChange(1, "1", "taken@ex.com"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("already registered"));
    }

    @Test
    void testConfirmEmailChange_InvalidOtp() {
        validUser.setPendingEmail("new@ex.com");
        validUser.setEmailChangeOtp("123456");
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));

        RuntimeException ex = assertThrows(RuntimeException.class, 
            () -> authService.confirmEmailChange(1, "1", "new@ex.com", "wrong"));
        assertTrue(ex.getMessage().contains("Invalid verification code"));
    }

    @Test
    void testRegisterUser_PublishFailure() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(validUser);
        doThrow(new RuntimeException("MQ Down")).when(authEventPublisher).publishOtpRequested(anyString(), anyString(), any());

        String resp = authService.registerUser(validUser);
        assertTrue(resp.contains("Registration successful")); // Still succeeds but logs warning
    }

    @Test
    void testRequestEmailChange_PublishFailure() {
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        doThrow(new RuntimeException("MQ Down")).when(authEventPublisher).publishOtpRequested(anyString(), anyString(), any());

        String resp = authService.requestEmailChange(1, "1", "new@ex.com");
        assertTrue(resp.contains("Verification code sent"));
    }

    @Test
    void testConfirmEmailChange_EmailMismatch() {
        validUser.setPendingEmail("new@ex.com");
        validUser.setEmailChangeOtp("123456");
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));

        assertThrows(RuntimeException.class, 
            () -> authService.confirmEmailChange(1, "1", "wrong@ex.com", "123456"));
    }

    @Test
    void testResetPassword_InvalidToken() {
        when(jwtUtil.validateToken("bad")).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> authService.resetPassword("bad", "newPassword123"));
    }

    @Test
    void testConfirmEmailChange_Expired() {
        validUser.setPendingEmail("new@ex.com");
        validUser.setEmailChangeOtp("123456");
        validUser.setEmailChangeOtpRequestedAt(LocalDateTime.now().minusMinutes(6));
        when(userRepository.findById(1)).thenReturn(Optional.of(validUser));

        RuntimeException ex = assertThrows(RuntimeException.class, 
            () -> authService.confirmEmailChange(1, "1", "new@ex.com", "123456"));
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void testRegisterUser_NullUser() {
        assertThrows(ResponseStatusException.class, () -> authService.registerUser(null));
    }

    @Test
    void testRegisterUser_EmptyEmail() {
        validUser.setEmail("");
        assertThrows(ResponseStatusException.class, () -> authService.registerUser(validUser));
    }

    @Test
    void testRegisterUser_EmptyPassword() {
        validUser.setPasswordHash("");
        assertThrows(ResponseStatusException.class, () -> authService.registerUser(validUser));
    }

    @Test
    void testVerifyOtp_EmptyEmail() {
        assertThrows(ResponseStatusException.class, () -> authService.verifyOtp("", "123456"));
    }

    @Test
    void testVerifyOtp_EmptyOtp() {
        assertThrows(ResponseStatusException.class, () -> authService.verifyOtp("test@ex.com", ""));
    }

    @Test
    void testVerifyOtp_UserNotFound() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> authService.verifyOtp("none@ex.com", "123456"));
    }

    @Test
    void testLogin_EmptyEmail() {
        assertThrows(ResponseStatusException.class, () -> authService.login("", "pass"));
    }

    @Test
    void testLogin_EmptyPassword() {
        assertThrows(ResponseStatusException.class, () -> authService.login("test@ex.com", ""));
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> authService.login("none@ex.com", "pass"));
    }

    @Test
    void testLogin_InvalidPassword() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(validUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> authService.login("john@example.com", "wrong"));
    }

    @Test
    void testLogout_InvalidHeader() {
        authService.logout(null);
        authService.logout("InvalidHeader");
        verify(tokenBlacklistRepository, never()).save(any());
    }

    @Test
    void testGetProfile_NullUserId() {
        assertThrows(ResponseStatusException.class, () -> authService.getProfile(null));
    }

    @Test
    void testUpdateProfile_EmptyFullName() {
        assertThrows(RuntimeException.class, () -> authService.updateProfile(1, "1", "", "123"));
    }

    @Test
    void testRequestEmailChange_EmptyEmail() {
        assertThrows(ResponseStatusException.class, () -> authService.requestEmailChange(1, "1", ""));
    }

    @Test
    void testConfirmEmailChange_EmptyOtp() {
        assertThrows(RuntimeException.class, () -> authService.confirmEmailChange(1, "1", "new@ex.com", ""));
    }

    @Test
    void testResetPassword_EmptyToken() {
        assertThrows(ResponseStatusException.class, () -> authService.resetPassword("", "newPass"));
    }

    @Test
    void testResetPassword_ShortPassword() {
        assertThrows(ResponseStatusException.class, () -> authService.resetPassword("token", "123"));
    }
}
