package com.helpmeout.task.dto;

import com.helpmeout.task.entity.ScheduledType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class UpdateTaskRequest {

    @NotNull
    private Long categoryId;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal budgetTnd;

    @NotBlank
    private String city;

    private String area;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    @NotNull
    private ScheduledType scheduledType;

    private OffsetDateTime scheduledAt;

    private String categorySpecificData;

    @Valid
    private List<TaskPhotoRequest> photos;
}

