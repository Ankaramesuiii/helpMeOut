package com.helpmeout.provider.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateProviderProfileRequest {

    @Size(max = 2000)
    private String description;

    @NotEmpty
    private List<Long> categoryIds;

    @Valid
    @NotEmpty
    private List<ProviderServiceAreaRequest> serviceAreas;

    @Valid
    @NotEmpty
    private List<ProviderAvailabilityRequest> availability;

    private Boolean isActive;
}

