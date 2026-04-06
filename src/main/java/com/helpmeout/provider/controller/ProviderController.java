package com.helpmeout.provider.controller;

import com.helpmeout.provider.dto.CreateProviderProfileRequest;
import com.helpmeout.provider.dto.ProviderProfileResponse;
import com.helpmeout.provider.dto.UpdateProviderProfileRequest;
import com.helpmeout.provider.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping("/me/profile")
    public ResponseEntity<ProviderProfileResponse> createMyProfile(
            @Valid @RequestBody CreateProviderProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(providerService.createMyProfile(request));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<ProviderProfileResponse> getMyProfile() {
        return ResponseEntity.ok(providerService.getMyProfile());
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ProviderProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProviderProfileRequest request
    ) {
        return ResponseEntity.ok(providerService.updateMyProfile(request));
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<ProviderProfileResponse> getPublicProfile(@PathVariable Long providerId) {
        return ResponseEntity.ok(providerService.getPublicProfile(providerId));
    }
}

