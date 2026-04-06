package com.helpmeout.provider.service;

import com.helpmeout.category.entity.Category;
import com.helpmeout.category.repository.CategoryRepository;
import com.helpmeout.common.exception.BadRequestException;
import com.helpmeout.common.exception.NotFoundException;
import com.helpmeout.common.util.SecurityUtils;
import com.helpmeout.provider.dto.*;
import com.helpmeout.provider.entity.*;
import com.helpmeout.provider.repository.*;
import com.helpmeout.user.entity.User;
import com.helpmeout.user.entity.UserProfile;
import com.helpmeout.user.repository.UserProfileRepository;
import com.helpmeout.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderProfileRepository providerProfileRepository;
    private final ProviderCategoryRepository providerCategoryRepository;
    private final ProviderServiceAreaRepository providerServiceAreaRepository;
    private final ProviderAvailabilityRepository providerAvailabilityRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public ProviderProfileResponse createMyProfile(CreateProviderProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (providerProfileRepository.existsByUserId(userId)) {
            throw new BadRequestException("Provider profile already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ProviderProfile providerProfile = new ProviderProfile();
        providerProfile.setUser(user);
        providerProfile.setDescription(request.getDescription());
        providerProfile.setIsActive(true);

        ProviderProfile savedProfile = providerProfileRepository.save(providerProfile);

        saveCategories(savedProfile, request.getCategoryIds());
        saveServiceAreas(savedProfile, request.getServiceAreas());
        saveAvailability(savedProfile, request.getAvailability());

        return getMyProfile();
    }

    @Transactional(readOnly = true)
    public ProviderProfileResponse getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();

        ProviderProfile providerProfile = providerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Provider profile not found"));

        return buildResponse(providerProfile);
    }

    @Transactional
    public ProviderProfileResponse updateMyProfile(UpdateProviderProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        ProviderProfile providerProfile = providerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Provider profile not found"));

        providerProfile.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            providerProfile.setIsActive(request.getIsActive());
        }

        providerProfileRepository.save(providerProfile);

        providerCategoryRepository.deleteByProviderProfileId(providerProfile.getId());
        providerServiceAreaRepository.deleteByProviderProfileId(providerProfile.getId());
        providerAvailabilityRepository.deleteByProviderProfileId(providerProfile.getId());

        saveCategories(providerProfile, request.getCategoryIds());
        saveServiceAreas(providerProfile, request.getServiceAreas());
        saveAvailability(providerProfile, request.getAvailability());

        return buildResponse(providerProfile);
    }

    @Transactional(readOnly = true)
    public ProviderProfileResponse getPublicProfile(Long providerId) {
        ProviderProfile providerProfile = providerProfileRepository.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Provider profile not found"));

        return buildResponse(providerProfile);
    }

    private void saveCategories(ProviderProfile providerProfile, List<Long> categoryIds) {
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        if (categories.size() != categoryIds.size()) {
            throw new BadRequestException("One or more categories are invalid");
        }

        for (Category category : categories) {
            ProviderCategory providerCategory = new ProviderCategory();
            providerCategory.setProviderProfile(providerProfile);
            providerCategory.setCategory(category);
            providerCategoryRepository.save(providerCategory);
        }
    }

    private void saveServiceAreas(ProviderProfile providerProfile, List<ProviderServiceAreaRequest> serviceAreas) {
        for (ProviderServiceAreaRequest request : serviceAreas) {
            ProviderServiceArea area = new ProviderServiceArea();
            area.setProviderProfile(providerProfile);
            area.setCity(request.getCity());
            area.setArea(request.getArea());
            area.setRadiusKm(request.getRadiusKm());
            providerServiceAreaRepository.save(area);
        }
    }

    private void saveAvailability(ProviderProfile providerProfile, List<ProviderAvailabilityRequest> availabilityRequests) {
        for (ProviderAvailabilityRequest request : availabilityRequests) {
            if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().equals(request.getStartTime())) {
                throw new BadRequestException("Availability end time must be after start time");
            }

            ProviderAvailability availability = new ProviderAvailability();
            availability.setProviderProfile(providerProfile);
            availability.setDay(request.getDay());
            availability.setStartTime(request.getStartTime());
            availability.setEndTime(request.getEndTime());
            availability.setIsActive(true);
            providerAvailabilityRepository.save(availability);
        }
    }

    private ProviderProfileResponse buildResponse(ProviderProfile providerProfile) {
        User user = providerProfile.getUser();
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("User profile not found"));

        List<ProviderCategoryResponse> categories = providerCategoryRepository.findByProviderProfileId(providerProfile.getId())
                .stream()
                .map(pc -> new ProviderCategoryResponse(
                        pc.getCategory().getId(),
                        pc.getCategory().getNameAr(),
                        pc.getCategory().getSlug()
                ))
                .toList();

        List<ProviderServiceAreaResponse> serviceAreas = providerServiceAreaRepository.findByProviderProfileId(providerProfile.getId())
                .stream()
                .map(a -> new ProviderServiceAreaResponse(
                        a.getCity(),
                        a.getArea(),
                        a.getRadiusKm()
                ))
                .toList();

        List<ProviderAvailabilityResponse> availability = providerAvailabilityRepository.findByProviderProfileId(providerProfile.getId())
                .stream()
                .map(a -> new ProviderAvailabilityResponse(
                        a.getDay().name(),
                        a.getStartTime().toString(),
                        a.getEndTime().toString()
                ))
                .toList();

        return ProviderProfileResponse.builder()
                .providerId(providerProfile.getId())
                .userId(user.getId())
                .fullName(userProfile.getFullName())
                .phone(user.getPhone())
                .profilePhotoUrl(userProfile.getProfilePhotoUrl())
                .city(userProfile.getCity())
                .area(userProfile.getArea())
                .bio(userProfile.getBio())
                .description(providerProfile.getDescription())
                .providerRating(providerProfile.getProviderRating())
                .completedTasksCount(providerProfile.getCompletedTasksCount())
                .isActive(providerProfile.getIsActive())
                .categories(categories)
                .serviceAreas(serviceAreas)
                .availability(availability)
                .build();
    }
}
