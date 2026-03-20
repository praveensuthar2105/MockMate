package com.mockmate.repository;

import com.mockmate.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionId(Long sessionId);

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
