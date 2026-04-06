package com.helpmeout.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPhoneRequest {

    @NotBlank
    private String phone;

    @NotBlank
    private String code;
}

