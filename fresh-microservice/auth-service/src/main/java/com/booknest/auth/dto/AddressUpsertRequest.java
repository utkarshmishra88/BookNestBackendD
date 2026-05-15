package com.booknest.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressUpsertRequest {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @Size(max = 50, message = "Label must be at most 50 characters")
    private String label;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 150, message = "Address line 1 must be at most 150 characters")
    private String line1;

    @Size(max = 150, message = "Address line 2 must be at most 150 characters")
    private String line2;

    @NotBlank(message = "City is required")
    @Size(max = 80, message = "City must be at most 80 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 80, message = "State must be at most 80 characters")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must be at most 20 characters")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(max = 80, message = "Country must be at most 80 characters")
    private String country;

    @NotBlank(message = "Mobile number is required for delivery")
    @Size(max = 20, message = "Mobile must be at most 20 characters")
    private String mobileNumber;

    @NotNull(message = "isDefault is required")
    private Boolean isDefault;
}