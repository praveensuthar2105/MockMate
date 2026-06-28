package com.mockmate.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import com.mockmate.agent.tool.SandboxExecutionTool;
import com.mockmate.agent.memory.DatabaseChatMemory;
import com.mockmate.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AgentFactory {

    private final Optional<ChatLanguageModel> chatLanguageModel;
    private final SandboxExecutionTool sandboxExecutionTool;
    private final ChatMessageRepository chatMessageRepository;

    public ResumeScreenAgent createResumeScreenAgent(Long sessionId, String systemPrompt) {
        if (chatLanguageModel.isEmpty()) {
            throw new IllegalStateException("ChatLanguageModel is not configured");
        }
        return AiServices.builder(ResumeScreenAgent.class)
                .chatLanguageModel(chatLanguageModel.get())
                .chatMemory(new DatabaseChatMemory(sessionId, chatMessageRepository))
                .systemMessageProvider(chatMemoryId -> systemPrompt)
                .build();
    }

    public DsaCodingAgent createDsaCodingAgent(Long sessionId, String systemPrompt) {
        if (chatLanguageModel.isEmpty()) {
            throw new IllegalStateException("ChatLanguageModel is not configured");
        }
        return AiServices.builder(DsaCodingAgent.class)
                .chatLanguageModel(chatLanguageModel.get())
                .chatMemory(new DatabaseChatMemory(sessionId, chatMessageRepository))
                .tools(sandboxExecutionTool)
                .systemMessageProvider(chatMemoryId -> systemPrompt)
                .build();
    }

    public SystemDesignAgent createSystemDesignAgent(Long sessionId, String systemPrompt) {
        if (chatLanguageModel.isEmpty()) {
            throw new IllegalStateException("ChatLanguageModel is not configured");
        }
        return AiServices.builder(SystemDesignAgent.class)
                .chatLanguageModel(chatLanguageModel.get())
                .chatMemory(new DatabaseChatMemory(sessionId, chatMessageRepository))
                .systemMessageProvider(chatMemoryId -> systemPrompt)
                .build();
    }

    public BehavioralStarAgent createBehavioralStarAgent(Long sessionId, String systemPrompt) {
        if (chatLanguageModel.isEmpty()) {
            throw new IllegalStateException("ChatLanguageModel is not configured");
        }
        return AiServices.builder(BehavioralStarAgent.class)
                .chatLanguageModel(chatLanguageModel.get())
                .chatMemory(new DatabaseChatMemory(sessionId, chatMessageRepository))
                .systemMessageProvider(chatMemoryId -> systemPrompt)
                .build();
    }

    public ScoringAgent createScoringAgent() {
        if (chatLanguageModel.isEmpty()) {
            throw new IllegalStateException("ChatLanguageModel is not configured");
        }
        return AiServices.builder(ScoringAgent.class)
                .chatLanguageModel(chatLanguageModel.get())
                .build();
    }
}
