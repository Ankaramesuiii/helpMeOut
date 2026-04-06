package com.helpmeout.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelTaskRequest {

    @NotBlank
    private String reason;
}