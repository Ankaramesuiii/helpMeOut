package com.helpmeout.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ChatResponse {
    private Long chatId;
    private Long taskId;
    private String taskTitle;
    private Long applicationId;
    private String status;
    private Long customerId;
    private String customerName;
    private Long providerId;
    private Long providerUserId;
    private String providerName;
    private String providerPhotoUrl;
    private String lastMessage;
    private OffsetDateTime lastMessageAt;
    private OffsetDateTime createdAt;
}

