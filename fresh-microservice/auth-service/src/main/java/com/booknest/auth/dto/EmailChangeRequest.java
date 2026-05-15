package com.booknest.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailChangeRequest {

    @NotBlank
    @Email
    private String newEmail;
}
