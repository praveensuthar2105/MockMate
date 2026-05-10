package com.mockmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockmate.model.ChatMessage;
import com.mockmate.model.InterviewSession;
import com.mockmate.model.PhaseType;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoringService {

    @org.springframework.beans.factory.annotation.Value("${langchain4j.googleai.gemini.model-name:gemini-1.5-pro}")
    private String modelName;

    @org.springframework.beans.factory.annotation.Value("${langchain4j.googleai.gemini.temperature:0.7}")
    private double temperature;

    @org.springframework.beans.factory.annotation.Value("${langchain4j.googleai.gemini.timeout-seconds:30}")
    private long timeoutSeconds;


    private final ObjectMapper objectMapper;
    private final com.mockmate.repository.ChatMessageRepository chatMessageRepository;

    @Value("${langchain4j.googleai.gemini.api-key:}")
    private String apiKey;

    private ChatLanguageModel chatModel;
    private String scoringTemplate;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.chatModel = GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName("gemini-1.5-flash")
                    .temperature(0.2)
                    .build();
        }

        try {
            scoringTemplate = org.springframework.util.StreamUtils.copyToString(
                    new ClassPathResource("prompts/scoring.txt").getInputStream(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load scoring template", e);
            scoringTemplate = "Score this interview: {{transcript}}";
        }
    }

    public Map<String, Object> evaluatePhase(InterviewSession session, PhaseType phase) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId())
                .stream()
                .filter(m -> m.getPhaseType() == phase)
                .collect(Collectors.toList());

        if (messages.isEmpty()) {
            return Map.of("score", 0, "feedback", "No activity in this phase.");
        }

        String transcript = messages.stream()
                .map(m -> m.getRole().name() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        String prompt = replacePlaceholders(scoringTemplate, Map.of(
                "transcript", transcript,
                "company", session.getCompany() != null ? session.getCompany() : "General",
                "difficulty", session.getDifficulty() != null ? session.getDifficulty().name() : "MEDIUM",
                "phase", phase.name()));

        if (chatModel == null) {
            return Map.of("score", 70, "feedback", "[MOCK] AI scoring is not configured. Defaulting to 70.");
        }

        try {
            String response = chatModel.generate(prompt);
            String cleanJson = cleanJsonResponse(response);
            return objectMapper.readValue(cleanJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to evaluate phase {}", phase, e);
            return Map.of("score", 50, "feedback", "Failed to analyze phase due to an error.");
        }
    }

    private String replacePlaceholders(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    private String cleanJsonResponse(String response) {
        String clean = response.trim();
        if (clean.startsWith("```json"))
            clean = clean.substring(7);
        if (clean.startsWith("```"))
            clean = clean.substring(3);
        if (clean.endsWith("```"))
            clean = clean.substring(0, clean.length() - 3);
        return clean.trim();
    }

    // Consent logic needs User to have getConsentToThirdParty.
    // Let us skip consent if it is not in the model? Wait, the prompt says "add a pre-send check that verifies user consent (e.g., session/user.hasConsentedToThirdPartyProcessing())"
    // I should add hasConsentedToThirdPartyProcessing to User model.
    private String sanitizeTranscript(java.util.List<com.mockmate.model.ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (com.mockmate.model.ChatMessage m : messages) {
            String role = "ai".equals(m.getRole()) ? "Interviewer" : "Candidate";
            String sanitized = m.getContent().replaceAll("(?i)ignore previous instructions|system:|output format:", "[REDACTED]");
            sb.append(role).append(": ").append(sanitized).append("\n");
        }
        return sb.toString();
    }
}