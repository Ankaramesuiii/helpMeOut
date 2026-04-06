package com.helpmeout.provider.repository;

import com.helpmeout.provider.entity.ProviderCategory;
import com.helpmeout.provider.entity.ProviderCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderCategoryRepository extends JpaRepository<ProviderCategory, ProviderCategoryId> {
    List<ProviderCategory> findByProviderProfileId(Long providerProfileId);
    void deleteByProviderProfileId(Long providerProfileId);
}
