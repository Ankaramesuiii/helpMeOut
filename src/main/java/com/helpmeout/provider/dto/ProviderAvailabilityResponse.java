package com.helpmeout.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProviderAvailabilityResponse {
    private String day;
    private String startTime;
    private String endTime;
}
