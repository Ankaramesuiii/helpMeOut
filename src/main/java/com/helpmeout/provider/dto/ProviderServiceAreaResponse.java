package com.helpmeout.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProviderServiceAreaResponse {
    private String city;
    private String area;
    private Integer radiusKm;
}
