package com.helpmeout.chat.controller;

import com.helpmeout.chat.dto.ChatMessageResponse;
import com.helpmeout.chat.dto.ChatResponse;
import com.helpmeout.chat.dto.SendMessageRequest;
import com.helpmeout.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/me")
    public ResponseEntity<List<ChatResponse>> getMyChats() {
        return ResponseEntity.ok(chatService.getMyChats());
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long chatId) {
        return ResponseEntity.ok(chatService.getMessages(chatId));
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        return ResponseEntity.ok(chatService.sendMessage(chatId, request));
    }
}

