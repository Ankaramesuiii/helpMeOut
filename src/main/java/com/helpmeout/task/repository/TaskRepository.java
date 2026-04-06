package com.helpmeout.task.repository;

import com.helpmeout.task.entity.Task;
import com.helpmeout.task.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Task> findByStatusInOrderByCreatedAtDesc(List<TaskStatus> statuses);

    List<Task> findByCategoryIdAndCityAndStatusInOrderByCreatedAtDesc(
            Long categoryId,
            String city,
            List<TaskStatus> statuses
    );
}

