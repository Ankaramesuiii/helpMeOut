package com.helpmeout.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderServiceAreaRequest {

    @NotBlank
    private String city;

    private String area;

    @NotNull
    private Integer radiusKm;
}
