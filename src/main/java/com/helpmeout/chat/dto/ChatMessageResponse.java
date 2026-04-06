package com.helpmeout.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String messageType;
    private String content;
    private OffsetDateTime readAt;
    private OffsetDateTime createdAt;
}

