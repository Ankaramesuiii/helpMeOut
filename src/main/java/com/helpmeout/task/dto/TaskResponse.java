package com.helpmeout.task.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class TaskResponse {
    private Long id;
    private Long customerId;
    private Long categoryId;
    private String categoryNameAr;
    private String title;
    private String description;
    private BigDecimal budgetTnd;
    private String city;
    private String area;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean exactLocationVisible;
    private String categorySpecificData;
    private String scheduledType;
    private OffsetDateTime scheduledAt;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<TaskPhotoResponse> photos;
}
