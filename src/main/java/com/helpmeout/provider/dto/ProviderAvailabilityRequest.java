package com.helpmeout.provider.dto;

import com.helpmeout.provider.entity.AvailabilityDay;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class ProviderAvailabilityRequest {

    @NotNull
    private AvailabilityDay day;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;
}

