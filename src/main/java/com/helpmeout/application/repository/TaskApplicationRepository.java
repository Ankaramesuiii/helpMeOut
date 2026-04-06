package com.helpmeout.application.repository;

import com.helpmeout.application.entity.ApplicationStatus;
import com.helpmeout.application.entity.TaskApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskApplicationRepository extends JpaRepository<TaskApplication, Long> {

    List<TaskApplication> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    List<TaskApplication> findByProviderProfileIdOrderByCreatedAtDesc(Long providerProfileId);

    Optional<TaskApplication> findByTaskIdAndProviderProfileId(Long taskId, Long providerProfileId);

    boolean existsByTaskIdAndProviderProfileId(Long taskId, Long providerProfileId);

    List<TaskApplication> findByTaskIdAndStatus(Long taskId, ApplicationStatus status);
}


