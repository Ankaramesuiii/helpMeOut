package com.helpmeout.provider.repository;

import com.helpmeout.provider.entity.ProviderServiceArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderServiceAreaRepository extends JpaRepository<ProviderServiceArea, Long> {
    List<ProviderServiceArea> findByProviderProfileId(Long providerProfileId);
    void deleteByProviderProfileId(Long providerProfileId);
}

