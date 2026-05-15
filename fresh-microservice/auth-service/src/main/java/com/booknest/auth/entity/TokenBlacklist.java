package com.booknest.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "token_blacklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Token is required")
    @Size(max = 512, message = "Token must not exceed 512 characters")
    @Column(unique = true, nullable = false, length = 512)
    private String token;
}