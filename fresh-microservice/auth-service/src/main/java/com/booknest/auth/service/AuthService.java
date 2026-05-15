package com.booknest.auth.service;

import com.booknest.auth.dto.UserResponse;
import com.booknest.auth.entity.User;

import java.util.List;

public interface AuthService {
    String registerUser(User user);
    String verifyOtp(String email, String otp);
    String login(String email, String password);
    void logout(String authHeader);
    User getProfile(Integer userId);

    User updateProfile(Integer userId, String principalUserId, String fullName, String mobileNumber);

    String requestEmailChange(Integer userId, String principalUserId, String newEmail);

    /** Returns new JWT after successful email verification. */
    String confirmEmailChange(Integer userId, String principalUserId, String newEmail, String otp);

    List<UserResponse> listUsersForAdmin();

    UserResponse setUserActive(Integer userId, Boolean active);

    String forgotPassword(String email);
    String resetPassword(String token, String newPassword);
}