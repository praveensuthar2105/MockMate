package com.mockmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockmate.model.ChatMessage;
import com.mockmate.model.InterviewSession;
import com.mockmate.model.PhaseType;
import com.mockmate.agent.AgentFactory;
import com.mockmate.agent.ScoringAgent;
import com.mockmate.dto.response.PhaseScore;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScoringService {

    private final com.mockmate.repository.ChatMessageRepository chatMessageRepository;
    private final ChatLanguageModel chatModel;
    private final AgentFactory agentFactory;

    public ScoringService(
            ObjectMapper objectMapper,
            com.mockmate.repository.ChatMessageRepository chatMessageRepository,
            Optional<ChatLanguageModel> chatLanguageModel,
            AgentFactory agentFactory) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatModel = chatLanguageModel.orElse(null);
        this.agentFactory = agentFactory;
    }

    public Map<String, Object> evaluatePhase(InterviewSession session, PhaseType phase) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId())
                .stream()
                .filter(m -> m.getPhaseType() == phase)
                .collect(Collectors.toList());

        if (messages.isEmpty()) {
            return Map.of(
                    "score", 0,
                    "feedback", "No activity in this phase.",
                    "strengths", java.util.Collections.emptyList(),
                    "weaknesses", java.util.Collections.emptyList(),
                    "recommendations", java.util.Collections.emptyList()
            );
        }

        String transcript = sanitizeTranscript(messages);

        if (chatModel == null) {
            return Map.of(
                    "score", 70,
                    "feedback", "[MOCK] AI scoring is not configured. Defaulting to 70.",
                    "strengths", List.of("Good candidate participation"),
                    "weaknesses", List.of("Refine technical depth"),
                    "recommendations", List.of("Practice more mock interviews")
            );
        }

        try {
            ScoringAgent scoringAgent = agentFactory.createScoringAgent();
            PhaseScore phaseScore = scoringAgent.evaluate(
                    session.getCompany() != null ? session.getCompany() : "General",
                    session.getDifficulty() != null ? session.getDifficulty().name() : "MEDIUM",
                    phase.name(),
                    transcript
            );

            // Null-safe check for list fields in case the LLM outputs empty or missing fields
            List<String> strengths = phaseScore.strengths() != null ? phaseScore.strengths() : java.util.Collections.emptyList();
            List<String> weaknesses = phaseScore.weaknesses() != null ? phaseScore.weaknesses() : java.util.Collections.emptyList();
            List<String> recommendations = phaseScore.recommendations() != null ? phaseScore.recommendations() : java.util.Collections.emptyList();

            return Map.of(
                    "score", phaseScore.score(),
                    "feedback", phaseScore.feedback() != null ? phaseScore.feedback() : "",
                    "strengths", strengths,
                    "weaknesses", weaknesses,
                    "recommendations", recommendations
            );
        } catch (Exception e) {
            log.error("Failed to evaluate phase {}", phase, e);
            return Map.of(
                    "score", 50,
                    "feedback", "Failed to analyze phase due to an error.",
                    "strengths", java.util.Collections.emptyList(),
                    "weaknesses", java.util.Collections.emptyList(),
                    "recommendations", java.util.Collections.emptyList()
            );
        }
    }

    private String sanitizeTranscript(java.util.List<com.mockmate.model.ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (com.mockmate.model.ChatMessage m : messages) {
            String role = m.getRole() == com.mockmate.model.Role.AI ? "Interviewer" : "Candidate";
            String sanitized = m.getContent().replaceAll("(?i)ignore previous instructions|system:|output format:", "[REDACTED]");
            sb.append(role).append(": ").append(sanitized).append("\n");
        }
        return sb.toString();
    }
}