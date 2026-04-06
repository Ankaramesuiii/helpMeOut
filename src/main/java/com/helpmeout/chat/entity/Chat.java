package com.helpmeout.chat.entity;

import com.helpmeout.application.entity.TaskApplication;
import com.helpmeout.common.entity.BaseEntity;
import com.helpmeout.provider.entity.ProviderProfile;
import com.helpmeout.task.entity.Task;
import com.helpmeout.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "chats")
public class Chat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_application_id")
    private TaskApplication taskApplication;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_profile_id", nullable = false)
    private ProviderProfile providerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatStatus status = ChatStatus.ACTIVE;
}