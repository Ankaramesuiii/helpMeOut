package com.helpmeout.chat.service;

import com.helpmeout.application.entity.TaskApplication;
import com.helpmeout.application.repository.TaskApplicationRepository;
import com.helpmeout.chat.dto.ChatMessageResponse;
import com.helpmeout.chat.dto.ChatResponse;
import com.helpmeout.chat.dto.SendMessageRequest;
import com.helpmeout.chat.entity.Chat;
import com.helpmeout.chat.entity.ChatMessage;
import com.helpmeout.chat.entity.ChatStatus;
import com.helpmeout.chat.repository.ChatMessageRepository;
import com.helpmeout.chat.repository.ChatRepository;
import com.helpmeout.common.exception.BadRequestException;
import com.helpmeout.common.exception.NotFoundException;
import com.helpmeout.common.util.SecurityUtils;
import com.helpmeout.provider.entity.ProviderProfile;
import com.helpmeout.task.entity.Task;
import com.helpmeout.user.entity.User;
import com.helpmeout.user.entity.UserProfile;
import com.helpmeout.user.repository.UserProfileRepository;
import com.helpmeout.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TaskApplicationRepository taskApplicationRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public Chat createChatForApplication(TaskApplication application) {
        return chatRepository.findByTaskApplicationId(application.getId())
                .orElseGet(() -> {
                    Chat chat = new Chat();
                    chat.setTask(application.getTask());
                    chat.setTaskApplication(application);
                    chat.setCustomer(application.getTask().getCustomer());
                    chat.setProviderProfile(application.getProviderProfile());
                    chat.setStatus(ChatStatus.ACTIVE);
                    return chatRepository.save(chat);
                });
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getMyChats() {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        return chatRepository.findByCustomerIdOrProviderProfileUserIdOrderByCreatedAtDesc(currentUserId, currentUserId)
                .stream()
                .map(this::mapToChatResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long chatId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found"));

        validateChatAccess(chat, currentUserId);

        return chatMessageRepository.findByChatIdOrderByCreatedAtAsc(chatId)
                .stream()
                .map(this::mapToMessageResponse)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long chatId, SendMessageRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new NotFoundException("Chat not found"));

        validateChatAccess(chat, currentUserId);

        if (chat.getStatus() != ChatStatus.ACTIVE) {
            throw new BadRequestException("Chat is closed");
        }

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ChatMessage message = new ChatMessage();
        message.setChat(chat);
        message.setSender(sender);
        message.setMessageType("TEXT");
        message.setContent(request.getContent());

        ChatMessage savedMessage = chatMessageRepository.save(message);

        return mapToMessageResponse(savedMessage);
    }

    @Transactional
    public void closeNonSelectedChatsForTask(Long taskId, Long selectedApplicationId) {
        List<Chat> chats = chatRepository.findByTaskIdAndStatus(taskId, ChatStatus.ACTIVE);

        for (Chat chat : chats) {
            if (chat.getTaskApplication() != null &&
                    !chat.getTaskApplication().getId().equals(selectedApplicationId)) {
                chat.setStatus(ChatStatus.CLOSED);
                chatRepository.save(chat);
            }
        }
    }

    private void validateChatAccess(Chat chat, Long currentUserId) {
        boolean isCustomer = chat.getCustomer().getId().equals(currentUserId);
        boolean isProvider = chat.getProviderProfile().getUser().getId().equals(currentUserId);

        if (!isCustomer && !isProvider) {
            throw new BadRequestException("You are not allowed to access this chat");
        }
    }

    private ChatResponse mapToChatResponse(Chat chat) {
        User customer = chat.getCustomer();
        ProviderProfile providerProfile = chat.getProviderProfile();
        User providerUser = providerProfile.getUser();

        UserProfile customerProfile = userProfileRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new NotFoundException("Customer profile not found"));

        UserProfile providerUserProfile = userProfileRepository.findByUserId(providerUser.getId())
                .orElseThrow(() -> new NotFoundException("Provider profile not found"));

        List<ChatMessage> messages = chatMessageRepository.findByChatIdOrderByCreatedAtAsc(chat.getId());

        String lastMessage = null;
        OffsetDateTime lastMessageAt = null;

        if (!messages.isEmpty()) {
            ChatMessage last = messages.get(messages.size() - 1);
            lastMessage = last.getContent();
            lastMessageAt = last.getCreatedAt();
        }

        return ChatResponse.builder()
                .chatId(chat.getId())
                .taskId(chat.getTask().getId())
                .taskTitle(chat.getTask().getTitle())
                .applicationId(chat.getTaskApplication() != null ? chat.getTaskApplication().getId() : null)
                .status(chat.getStatus().name())
                .customerId(customer.getId())
                .customerName(customerProfile.getFullName())
                .providerId(providerProfile.getId())
                .providerUserId(providerUser.getId())
                .providerName(providerUserProfile.getFullName())
                .providerPhotoUrl(providerUserProfile.getProfilePhotoUrl())
                .lastMessage(lastMessage)
                .lastMessageAt(lastMessageAt)
                .createdAt(chat.getCreatedAt())
                .build();
    }

    private ChatMessageResponse mapToMessageResponse(ChatMessage message) {
        UserProfile senderProfile = userProfileRepository.findByUserId(message.getSender().getId())
                .orElseThrow(() -> new NotFoundException("Sender profile not found"));

        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .senderName(senderProfile.getFullName())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}

