package com.booknest.auth.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Integer userId;
    private String fullName;
    private String email;
    private String role;
    private String provider;
    private Boolean active;
    private String mobileNumber;
    private LocalDateTime createdAt;
}