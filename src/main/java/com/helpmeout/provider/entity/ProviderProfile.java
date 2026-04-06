package com.helpmeout.provider.entity;

import com.helpmeout.common.entity.BaseEntity;
import com.helpmeout.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "provider_profiles")
public class ProviderProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "provider_rating", nullable = false)
    private Double providerRating = 0.0;

    @Column(name = "completed_tasks_count", nullable = false)
    private Integer completedTasksCount = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}