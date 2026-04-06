package com.helpmeout.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ApplyToTaskRequest {

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal proposedPrice;

    @Size(max = 2000)
    private String applicationMessage;
}

