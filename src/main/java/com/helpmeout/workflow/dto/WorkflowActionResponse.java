package com.helpmeout.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WorkflowActionResponse {
    private Long taskId;
    private String status;
    private String message;
}