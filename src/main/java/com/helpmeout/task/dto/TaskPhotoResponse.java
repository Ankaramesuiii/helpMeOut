package com.helpmeout.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskPhotoResponse {
    private Long id;
    private String imageUrl;
    private Boolean isPrimary;
}