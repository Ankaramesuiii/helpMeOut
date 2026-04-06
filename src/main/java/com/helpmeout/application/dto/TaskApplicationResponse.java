package com.helpmeout.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class TaskApplicationResponse {
    private Long applicationId;
    private Long taskId;
    private String taskTitle;
    private String status;
    private BigDecimal proposedPrice;
    private String applicationMessage;
    private OffsetDateTime createdAt;

    private Long providerId;
    private Long providerUserId;
    private String providerFullName;
    private String providerPhotoUrl;
    private Double providerRating;
    private Integer completedTasksCount;
    private List<String> providerCategories;
    private String providerCity;
    private String providerArea;
}
