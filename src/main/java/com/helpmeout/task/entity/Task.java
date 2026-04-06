package com.helpmeout.task.entity;

import com.helpmeout.category.entity.Category;
import com.helpmeout.common.entity.AuditableEntity;
import com.helpmeout.provider.entity.ProviderProfile;
import com.helpmeout.user.entity.User;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "tasks")
public class Task extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "budget_tnd", nullable = false, precision = 10, scale = 2)
    private BigDecimal budgetTnd;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "area", length = 100)
    private String area;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "exact_location_visible", nullable = false)
    private Boolean exactLocationVisible = false;

    @Type(JsonType.class)
    @Column(name = "category_specific_data", columnDefinition = "jsonb")
    private String categorySpecificData;

    @Enumerated(EnumType.STRING)
    @Column(name = "scheduled_type", nullable = false, length = 20)
    private ScheduledType scheduledType = ScheduledType.ASAP;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TaskStatus status = TaskStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_provider_id")
    private ProviderProfile selectedProvider;
}
