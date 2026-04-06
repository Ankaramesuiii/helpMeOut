package com.helpmeout.workflow.controller;

import com.helpmeout.workflow.dto.WorkflowActionResponse;
import com.helpmeout.workflow.service.TaskWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/task-workflow/tasks")
@RequiredArgsConstructor
public class TaskWorkflowController {

    private final TaskWorkflowService taskWorkflowService;

    @PostMapping("/{taskId}/start")
    public ResponseEntity<WorkflowActionResponse> startTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskWorkflowService.startTask(taskId));
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<WorkflowActionResponse> completeTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskWorkflowService.completeTask(taskId));
    }

    @PostMapping("/{taskId}/confirm-completion")
    public ResponseEntity<WorkflowActionResponse> confirmCompletion(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskWorkflowService.confirmCompletion(taskId));
    }

    @PostMapping("/{taskId}/mark-paid")
    public ResponseEntity<WorkflowActionResponse> markPaid(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskWorkflowService.markPaid(taskId));
    }

    @PostMapping("/{taskId}/confirm-payment")
    public ResponseEntity<WorkflowActionResponse> confirmPayment(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskWorkflowService.confirmPayment(taskId));
    }
}