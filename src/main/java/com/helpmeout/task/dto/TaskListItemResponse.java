package com.helpmeout.task.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class TaskListItemResponse {
    private Long id;
    private String title;
    private String categoryNameAr;
    private BigDecimal budgetTnd;
    private String city;
    private String area;
    private String description;
    private String status;
    private String scheduledType;
    private OffsetDateTime scheduledAt;
    private OffsetDateTime createdAt;
}

