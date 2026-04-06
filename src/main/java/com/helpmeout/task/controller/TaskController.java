package com.helpmeout.task.controller;

import com.helpmeout.task.dto.*;
import com.helpmeout.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TaskListItemResponse>> getMyTasks() {
        return ResponseEntity.ok(taskService.getMyTasks());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @GetMapping
    public ResponseEntity<List<TaskListItemResponse>> browseTasks(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city
    ) {
        return ResponseEntity.ok(taskService.browseOpenTasks(categoryId, city));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request));
    }

    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<Void> cancelTask(
            @PathVariable Long taskId,
            @Valid @RequestBody CancelTaskRequest request
    ) {
        taskService.cancelTask(taskId, request);
        return ResponseEntity.noContent().build();
    }
}

