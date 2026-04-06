package com.helpmeout.provider.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProviderProfileResponse {
    private Long providerId;
    private Long userId;
    private String fullName;
    private String phone;
    private String profilePhotoUrl;
    private String city;
    private String area;
    private String bio;
    private String description;
    private Double providerRating;
    private Integer completedTasksCount;
    private Boolean isActive;
    private List<ProviderCategoryResponse> categories;
    private List<ProviderServiceAreaResponse> serviceAreas;
    private List<ProviderAvailabilityResponse> availability;
}

