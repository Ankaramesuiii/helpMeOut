package com.helpmeout.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskPhotoRequest {

    @NotBlank
    private String imageUrl;

    private Boolean isPrimary = false;
}
