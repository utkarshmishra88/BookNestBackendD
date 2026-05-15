package com.booknest.auth.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUpdateRequestedEvent {
    private String email;
    private String subject;
    private String message;
    private String mobileNumber;
}
