package com.booknest.auth.service.impl;

import com.booknest.auth.client.NotificationClient;
import com.booknest.auth.dto.UserResponse;
import com.booknest.auth.entity.TokenBlacklist;
import com.booknest.auth.entity.User;
import com.booknest.auth.repository.TokenBlacklistRepository;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.messaging.AuthEventPublisher;
import com.booknest.auth.service.AuthService;
import com.booknest.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final NotificationClient notificationClient;
    private final AuthEventPublisher authEventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public String registerUser(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid registration request.");
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required.");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists!");
        }

        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setOtp(otp);
        user.setOtpRequestedAt(LocalDateTime.now());
        user.setActive(false);
        user.setRole("CUSTOMER");
        user.setProvider("LOCAL");

        userRepository.save(user);
        try {
            authEventPublisher.publishOtpRequested(user.getEmail(), otp, user.getMobileNumber());
        } catch (Exception e) {
            log.warn("Registration OTP could not be sent to {}: {}", user.getEmail(), e.getMessage());
        }

        return "Registration successful! Please check your email for the OTP.";
    }

    @Override
    @Transactional
    public String verifyOtp(String email, String otp) {
        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }

        if (otp == null || otp.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is required.");
        }

        User user = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        if (user.getOtp() != null && user.getOtp().equals(otp)) {
            if (user.getOtpRequestedAt() == null ||
                    Duration.between(user.getOtpRequestedAt(), LocalDateTime.now()).toMinutes() >= 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired. Please request a new one.");
            }
            user.setActive(true);
            user.setOtp(null);
            user.setOtpRequestedAt(null);
            userRepository.save(user);
            return "Account verified! You can now login.";
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP.");
    }

    @Override
    public String login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Please verify your account via OTP first.");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        boolean matches;
        try {
            matches = passwordEncoder.matches(password, user.getPasswordHash());
        } catch (IllegalArgumentException ex) {
            log.warn("Stored password hash is invalid for user id {}: {}", user.getUserId(), ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        if (!matches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        return jwtUtil.generateToken(user);
    }

    @Override
    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring(7).trim();
        if (!token.isEmpty() && !tokenBlacklistRepository.existsByToken(token)) {
            tokenBlacklistRepository.save(
                    TokenBlacklist.builder()
                            .token(token)
                            .build()
            );
        }
    }

    @Override
    public User getProfile(Integer userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required.");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }

    private void assertSelf(Integer userId, String principalUserId) {
        if (principalUserId == null || !String.valueOf(userId).equals(principalUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own profile.");
        }
    }

    @Override
    @Transactional
    public User updateProfile(Integer userId, String principalUserId, String fullName, String mobileNumber) {
        assertSelf(userId, principalUserId);
        if (fullName == null || fullName.isBlank()) {
            throw new RuntimeException("Full name is required.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        user.setFullName(fullName.trim());
        if (mobileNumber != null) {
            user.setMobileNumber(mobileNumber.trim());
        }
        return userRepository.save(user);
    }

    /**
     * Not @Transactional: if the notification Feign call fails, the OTP must stay saved.
     * Otherwise the transaction rolls back and the client always sees a 500.
     */
    @Override
    public String requestEmailChange(Integer userId, String principalUserId, String newEmail) {
        assertSelf(userId, principalUserId);
        if (newEmail == null || newEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New email is required.");
        }
        String normalized = newEmail.trim().toLowerCase();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        if (normalized.equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This is already your current email.");
        }
        if (userRepository.existsByEmail(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This email is already registered to another account.");
        }

        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        user.setPendingEmail(normalized);
        user.setEmailChangeOtp(otp);
        user.setEmailChangeOtpRequestedAt(LocalDateTime.now());
        userRepository.save(user);

        try {
            authEventPublisher.publishOtpRequested(normalized, otp, user.getMobileNumber());
        } catch (Exception e) {
            log.warn("Email OTP could not be sent to {} (OTP still saved for verification): {}", normalized, e.getMessage());
        }
        return "Verification code sent to " + normalized;
    }

    @Override
    @Transactional
    public String confirmEmailChange(Integer userId, String principalUserId, String newEmail, String otp) {
        assertSelf(userId, principalUserId);
        if (otp == null || otp.isBlank()) {
            throw new RuntimeException("OTP is required.");
        }
        String normalized = newEmail != null ? newEmail.trim().toLowerCase() : "";
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.getPendingEmail() == null || user.getEmailChangeOtp() == null) {
            throw new RuntimeException("No pending email change. Request a new code first.");
        }
        if (!normalized.equalsIgnoreCase(user.getPendingEmail())) {
            throw new RuntimeException("Email does not match the pending change.");
        }
        if (!user.getEmailChangeOtp().equals(otp.trim())) {
            throw new RuntimeException("Invalid verification code.");
        }
        if (user.getEmailChangeOtpRequestedAt() == null ||
                Duration.between(user.getEmailChangeOtpRequestedAt(), LocalDateTime.now()).toMinutes() >= 5) {
            throw new RuntimeException("Verification code has expired. Please request a new one.");
        }

        user.setEmail(normalized);
        user.setPendingEmail(null);
        user.setEmailChangeOtp(null);
        user.setEmailChangeOtpRequestedAt(null);
        userRepository.save(user);
        return jwtUtil.generateToken(user);
    }

    @Override
    public List<UserResponse> listUsersForAdmin() {
        return userRepository.findAll().stream()
                .map(this::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse setUserActive(Integer userId, Boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        if (active != null) {
            user.setActive(active);
        }
        userRepository.save(user);
        return toUserResponseDto(user);
    }

    @Override
    public String forgotPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this email not found."));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not verified.");
        }

        // Generate a 15 minute token
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("email", user.getEmail());
        claims.put("purpose", "PASSWORD_RESET");

        String token = jwtUtil.generateToken(claims, 15 * 60 * 1000); // 15 minutes
        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        try {
            authEventPublisher.publishUpdateRequested(
                    user.getEmail(),
                    "Password Reset Request",
                    "You requested a password reset. Click the link below to set a new password. This link expires in 15 minutes.\n\n" + resetLink,
                    user.getMobileNumber()
            );
        } catch (Exception e) {
            log.warn("Password reset email could not be sent to {}: {}", user.getEmail(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send reset email.");
        }

        return "Password reset link has been sent to your email.";
    }

    @Override
    @Transactional
    public String resetPassword(String token, String newPassword) {
        if (token == null || token.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required.");
        }
        if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters long.");
        }

        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired reset token.");
        }

        String purpose = jwtUtil.extractClaim(token, claims -> claims.get("purpose", String.class));
        if (!"PASSWORD_RESET".equals(purpose)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token purpose.");
        }

        String email = jwtUtil.extractEmail(token);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token claims.");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Blacklist the token so it can't be used again
        tokenBlacklistRepository.save(
                TokenBlacklist.builder()
                        .token(token)
                        .build()
        );

        return "Password successfully reset! You can now log in.";
    }

    private UserResponse toUserResponseDto(User user) {
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