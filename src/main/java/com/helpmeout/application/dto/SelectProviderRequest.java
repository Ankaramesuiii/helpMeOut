package com.helpmeout.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelectProviderRequest {

    @NotNull
    private Long applicationId;
}