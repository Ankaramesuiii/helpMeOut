package com.helpmeout.provider.repository;

import com.helpmeout.provider.entity.ProviderAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderAvailabilityRepository extends JpaRepository<ProviderAvailability, Long> {
    List<ProviderAvailability> findByProviderProfileId(Long providerProfileId);
    void deleteByProviderProfileId(Long providerProfileId);
}

