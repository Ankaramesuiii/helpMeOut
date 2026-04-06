package com.helpmeout.category.entity;

import com.helpmeout.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(name = "name_ar", nullable = false, unique = true, length = 100)
    private String nameAr;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "form_config", columnDefinition = "jsonb")
    private String formConfig;
}

