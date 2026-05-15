package com.booknest.auth.resource;

import com.booknest.auth.dto.*;
import com.booknest.auth.entity.User;
import com.booknest.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(request.getPassword())
                .build();

        return new ResponseEntity<>(authService.registerUser(user), HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request.getEmail(), request.getOtp()));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok("Logged out successfully.");
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable Integer userId) {
        User user = authService.getProfile(userId);
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Integer userId,
                                                      @Valid @RequestBody UpdateProfileRequest body,
                                                      Authentication authentication) {
        String principal = authentication != null ? String.valueOf(authentication.getPrincipal()) : null;
        User user = authService.updateProfile(userId, principal, body.getFullName(), body.getMobileNumber());
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PostMapping("/profile/{userId}/email/request")
    public ResponseEntity<String> requestEmailChange(@PathVariable Integer userId,
                                                       @Valid @RequestBody EmailChangeRequest body,
                                                       Authentication authentication) {
        String principal = authentication != null ? String.valueOf(authentication.getPrincipal()) : null;
        return ResponseEntity.ok(authService.requestEmailChange(userId, principal, body.getNewEmail()));
    }

    @PostMapping("/profile/{userId}/email/confirm")
    public ResponseEntity<String> confirmEmailChange(@PathVariable Integer userId,
                                                     @Valid @RequestBody EmailConfirmRequest body,
                                                     Authentication authentication) {
        String principal = authentication != null ? String.valueOf(authentication.getPrincipal()) : null;
        String token = authService.confirmEmailChange(userId, principal, body.getNewEmail(), body.getOtp());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request.getToken(), request.getNewPassword()));
    }

    @GetMapping("/debug-auth")
    public ResponseEntity<Object> debugAuth(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok("No authentication found in context.");
        }
        Map<String, Object> debugInfo = new java.util.HashMap<>();
        debugInfo.put("principal", authentication.getPrincipal());
        debugInfo.put("authorities", authentication.getAuthorities());
        debugInfo.put("details", authentication.getDetails());
        return ResponseEntity.ok(debugInfo);
    }

    private static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .provider(user.getProvider())
                .active(user.getActive())
                .mobileNumber(user.getMobileNumber())
                .createdAt(user.getCreatedAt())
                .build();
    }
}