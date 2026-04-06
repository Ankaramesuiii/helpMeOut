package com.helpmeout.task.repository;

import com.helpmeout.task.entity.TaskPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskPhotoRepository extends JpaRepository<TaskPhoto, Long> {
    List<TaskPhoto> findByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}

