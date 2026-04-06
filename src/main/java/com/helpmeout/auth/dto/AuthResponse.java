package com.helpmeout.auth.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private Long userId;
    private String phone;
    private String fullName;
    private String token;
    private String role;
    private String phoneVerificationStatus;
}