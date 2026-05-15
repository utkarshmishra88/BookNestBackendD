package com.booknest.auth.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthOtpRequestedEvent {
    private String email;
    private String otp;
    private String mobileNumber;
}
