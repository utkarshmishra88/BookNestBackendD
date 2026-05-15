package com.booknest.auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private Integer userId;
    private String email;
    private String fullName;
    private String role;
}