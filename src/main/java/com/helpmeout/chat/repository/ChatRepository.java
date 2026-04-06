package com.helpmeout.chat.repository;

import com.helpmeout.chat.entity.Chat;
import com.helpmeout.chat.entity.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByTaskApplicationId(Long taskApplicationId);

    List<Chat> findByCustomerIdOrProviderProfileUserIdOrderByCreatedAtDesc(Long customerId, Long providerUserId);

    List<Chat> findByTaskId(Long taskId);

    List<Chat> findByTaskIdAndStatus(Long taskId, ChatStatus status);
}
