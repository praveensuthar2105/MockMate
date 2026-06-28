package com.mockmate.service;

import com.mockmate.agent.AgentFactory;
import com.mockmate.agent.DsaCodingAgent;
import com.mockmate.agent.ResumeScreenAgent;
import com.mockmate.agent.SystemDesignAgent;
import com.mockmate.agent.BehavioralStarAgent;
import com.mockmate.agent.tool.SandboxExecutionTool;
import com.mockmate.repository.ChatMessageRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AgentFactoryTest {

    @Mock
    private ChatLanguageModel chatLanguageModel;

    @Mock
    private SandboxExecutionTool sandboxExecutionTool;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    private AgentFactory agentFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        agentFactory = new AgentFactory(
                Optional.of(chatLanguageModel),
                sandboxExecutionTool,
                chatMessageRepository
        );
    }

    @Test
    void testCreateResumeScreenAgent() {
        ResumeScreenAgent agent = agentFactory.createResumeScreenAgent(1L, "System Prompt");
        assertNotNull(agent);
    }

    @Test
    void testCreateDsaCodingAgent() {
        DsaCodingAgent agent = agentFactory.createDsaCodingAgent(1L, "System Prompt");
        assertNotNull(agent);
    }

    @Test
    void testCreateSystemDesignAgent() {
        SystemDesignAgent agent = agentFactory.createSystemDesignAgent(1L, "System Prompt");
        assertNotNull(agent);
    }

    @Test
    void testCreateBehavioralStarAgent() {
        BehavioralStarAgent agent = agentFactory.createBehavioralStarAgent(1L, "System Prompt");
        assertNotNull(agent);
    }
}
