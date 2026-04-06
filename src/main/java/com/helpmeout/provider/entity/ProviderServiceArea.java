package com.helpmeout.provider.entity;

import com.helpmeout.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "provider_service_areas")
public class ProviderServiceArea extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_profile_id", nullable = false)
    private ProviderProfile providerProfile;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "area", length = 100)
    private String area;

    @Column(name = "radius_km")
    private Integer radiusKm = 10;
}

