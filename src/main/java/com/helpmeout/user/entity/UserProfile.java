package com.helpmeout.user.entity;

import com.helpmeout.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String fullName;

    private String profilePhotoUrl;

    @Column(nullable = false)
    private String city;

    private String area;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private Double customerRating = 0.0;

    @Column(nullable = false)
    private Integer customerCompletedTasks = 0;
}
