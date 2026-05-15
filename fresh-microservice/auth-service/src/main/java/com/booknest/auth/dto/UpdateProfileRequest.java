package com.booknest.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Name is too long")
    private String fullName;

    private String mobileNumber;
}
