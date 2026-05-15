package com.booknest.auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Integer addressId;
    private Integer userId;
    private String label;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String mobileNumber;
    private Boolean isDefault;
}