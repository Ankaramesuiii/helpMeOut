package com.helpmeout.application.controller;

import com.helpmeout.application.dto.ApplyToTaskRequest;
import com.helpmeout.application.dto.SelectProviderRequest;
import com.helpmeout.application.dto.TaskApplicationResponse;
import com.helpmeout.application.service.TaskApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task-applications")
@RequiredArgsConstructor
public class TaskApplicationController {

    private final TaskApplicationService taskApplicationService;

    @PostMapping("/tasks/{taskId}/apply")
    public ResponseEntity<TaskApplicationResponse> applyToTask(
            @PathVariable Long taskId,
            @Valid @RequestBody ApplyToTaskRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskApplicationService.applyToTask(taskId, request));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<List<TaskApplicationResponse>> getApplicationsForMyTask(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(taskApplicationService.getApplicationsForMyTask(taskId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<TaskApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(taskApplicationService.getMyApplications());
    }

    @PostMapping("/tasks/{taskId}/select")
    public ResponseEntity<TaskApplicationResponse> selectProviderForTask(
            @PathVariable Long taskId,
            @Valid @RequestBody SelectProviderRequest request
    ) {
        return ResponseEntity.ok(taskApplicationService.selectProviderForTask(taskId, request));
    }

    @PostMapping("/{applicationId}/withdraw")
    public ResponseEntity<Void> withdrawMyApplication(@PathVariable Long applicationId) {
        taskApplicationService.withdrawMyApplication(applicationId);
        return ResponseEntity.noContent().build();
    }
}