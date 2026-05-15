package com.booknest.auth.resource;

import com.booknest.auth.dto.*;
import com.booknest.auth.entity.User;
import com.booknest.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthResourceTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthResource authResource;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authResource).build();
    }

    @Test
    void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("Password@123");

        when(authService.registerUser(any(User.class))).thenReturn("Registration successful! Please check your email for the OTP.");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Registration successful! Please check your email for the OTP."));
    }

    @Test
    void testVerify() throws Exception {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setEmail("john@example.com");
        request.setOtp("123456");

        when(authService.verifyOtp(anyString(), anyString())).thenReturn("Account verified! You can now login.");

        mockMvc.perform(post("/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account verified! You can now login."));
    }

    @Test
    void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(authService.login(anyString(), anyString())).thenReturn("mockJwtToken");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("mockJwtToken"));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer mockJwtToken"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully."));
    }

    @Test
    void testForgotPassword() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("john@example.com");

        when(authService.forgotPassword("john@example.com")).thenReturn("OTP sent");

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent"));
    }

    @Test
    void testResetPassword() throws Exception {
        com.booknest.auth.dto.ResetPasswordRequest request = new com.booknest.auth.dto.ResetPasswordRequest();
        request.setToken("token123");
        request.setNewPassword("NewPass@123");

        when(authService.resetPassword("token123", "NewPass@123")).thenReturn("Password reset successful");

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successful"));
    }

    @Test
    void testGetProfile() throws Exception {
        User user = User.builder().userId(1).fullName("John").email("john@ex.com").build();
        when(authService.getProfile(1)).thenReturn(user);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/profile/1"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.fullName").value("John"));
    }

    @Test
    void testUpdateProfile() throws Exception {
        com.booknest.auth.dto.UpdateProfileRequest req = new com.booknest.auth.dto.UpdateProfileRequest();
        req.setFullName("Jane");
        when(authService.updateProfile(anyInt(), anyString(), anyString(), any())).thenReturn(User.builder().userId(1).fullName("Jane").build());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/auth/profile/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(new UsernamePasswordAuthenticationToken("1", null)))
                .andExpect(status().isOk());
    }

    @Test
    void testRequestEmailChange() throws Exception {
        com.booknest.auth.dto.EmailChangeRequest req = new com.booknest.auth.dto.EmailChangeRequest();
        req.setNewEmail("new@ex.com");
        when(authService.requestEmailChange(eq(1), anyString(), eq("new@ex.com"))).thenReturn("Code sent");

        mockMvc.perform(post("/auth/profile/1/email/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(new UsernamePasswordAuthenticationToken("1", null)))
                .andExpect(status().isOk());
    }

    @Test
    void testConfirmEmailChange() throws Exception {
        EmailConfirmRequest req = new EmailConfirmRequest();
        req.setNewEmail("new@ex.com");
        req.setOtp("123456");
        when(authService.confirmEmailChange(eq(1), anyString(), eq("new@ex.com"), eq("123456"))).thenReturn("newToken");

        mockMvc.perform(post("/auth/profile/1/email/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(new UsernamePasswordAuthenticationToken("1", null)))
                .andExpect(status().isOk())
                .andExpect(content().string("newToken"));
    }
}
