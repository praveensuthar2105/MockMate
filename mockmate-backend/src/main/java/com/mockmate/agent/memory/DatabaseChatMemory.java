package com.mockmate.agent.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import com.mockmate.repository.ChatMessageRepository;
import com.mockmate.model.Role;
import java.util.ArrayList;
import java.util.List;

public class DatabaseChatMemory implements ChatMemory {

    private final Long sessionId;
    private final ChatMessageRepository chatMessageRepository;
    private final List<ChatMessage> transientMessages = new ArrayList<>();

    public DatabaseChatMemory(Long sessionId, ChatMessageRepository chatMessageRepository) {
        this.sessionId = sessionId;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public Object id() {
        return sessionId;
    }

    @Override
    public List<ChatMessage> messages() {
        List<com.mockmate.model.ChatMessage> dbMessages = 
            chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        
        List<ChatMessage> result = new ArrayList<>();
        for (com.mockmate.model.ChatMessage msg : dbMessages) {
            if (msg.getRole() == Role.USER) {
                result.add(dev.langchain4j.data.message.UserMessage.from(msg.getContent()));
            } else if (msg.getRole() == Role.AI) {
                result.add(dev.langchain4j.data.message.AiMessage.from(msg.getContent()));
            }
        }
        result.addAll(transientMessages);
        return result;
    }

    @Override
    public void add(ChatMessage message) {
        if (message instanceof dev.langchain4j.data.message.ToolExecutionResultMessage ||
            (message instanceof dev.langchain4j.data.message.AiMessage && ((dev.langchain4j.data.message.AiMessage) message).hasToolExecutionRequests())) {
            transientMessages.add(message);
        }
    }

    @Override
    public void clear() {
        transientMessages.clear();
    }
}
