package com.mockmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockmate.dto.code.DsaProblem;
import com.mockmate.dto.response.ChatResponse;
import com.mockmate.model.*;
import com.mockmate.repository.ChatMessageRepository;
import com.mockmate.repository.CodeSubmissionRepository;
import com.mockmate.repository.InterviewSessionRepository;
import com.mockmate.repository.ResumeRepository;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

        private final ChatLanguageModel chatLanguageModel;
        private final ChatMessageRepository chatMessageRepository;
        private final InterviewSessionRepository sessionRepository;
        private final ResumeRepository resumeRepository;
        private final CodeSubmissionRepository codeSubmissionRepository;
        private final ObjectMapper objectMapper;

        private String baseContextTemplate;
        private String resumeScreenTemplate;
        private String dsaTemplate;
        private String systemDesignTemplate;
        private String hrTemplate;

        @PostConstruct
        public void loadPromptTemplates() {
                baseContextTemplate = loadTemplate("prompts/base-context.txt");
                resumeScreenTemplate = loadTemplate("prompts/resume-screen.txt");
                dsaTemplate = loadTemplate("prompts/dsa.txt");
                systemDesignTemplate = loadTemplate("prompts/system-design.txt");
                hrTemplate = loadTemplate("prompts/hr.txt");
                log.info("All prompt templates loaded successfully");
        }

        private String loadTemplate(String path) {
                try {
                        String content = new ClassPathResource(path)
                                        .getContentAsString(Objects.requireNonNull(StandardCharsets.UTF_8));
                        return Objects.requireNonNull(content, "Template content must not be null");
                } catch (IOException e) {
                        log.error("Failed to load prompt template: {}", path, e);
                        throw new RuntimeException("Failed to load prompt template: " + path, e);
                }
        }

        @Transactional
        public ChatResponse processMessage(Long sessionId, String userMessage) {
                Objects.requireNonNull(sessionId, "sessionId must not be null");
                InterviewSession session = sessionRepository.findById(sessionId)
                                .orElseThrow(() -> new RuntimeException("Interview session not found"));

                if (session.getStatus() == SessionStatus.COMPLETED) {
                        throw new RuntimeException("Interview session is already completed");
                }

                // Save user message
                ChatMessage userMsg = new ChatMessage();
                userMsg.setSession(session);
                userMsg.setRole(Role.USER);
                userMsg.setContent(userMessage);
                userMsg.setPhaseType(session.getCurrentPhase());
                chatMessageRepository.save(userMsg);

                // Build conversation history and get AI response
                String aiResponseText = getAiResponse(session, userMessage);

                // Save AI response
                ChatMessage aiMsg = new ChatMessage();
                aiMsg.setSession(session);
                aiMsg.setRole(Role.AI);
                aiMsg.setContent(aiResponseText);
                aiMsg.setPhaseType(session.getCurrentPhase());
                ChatMessage savedAiMsg = chatMessageRepository.save(aiMsg);

                log.info("Chat processed for session {}, phase {}", sessionId, session.getCurrentPhase());

                return ChatResponse.builder()
                                .id(savedAiMsg.getId())
                                .role(Role.AI)
                                .content(aiResponseText)
                                .phaseType(session.getCurrentPhase())
                                .createdAt(savedAiMsg.getCreatedAt())
                                .build();
        }

        public List<ChatResponse> getChatHistory(Long sessionId) {
                return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                                .map(msg -> ChatResponse.builder()
                                                .id(msg.getId())
                                                .role(msg.getRole())
                                                .content(msg.getContent())
                                                .phaseType(msg.getPhaseType())
                                                .createdAt(msg.getCreatedAt())
                                                .build())
                                .toList();
        }

        @Transactional
        public ChatMessage saveAiMessage(InterviewSession session, String aiResponseText) {
                ChatMessage aiMsg = new ChatMessage();
                aiMsg.setSession(session);
                aiMsg.setRole(Role.AI);
                aiMsg.setContent(aiResponseText);
                aiMsg.setPhaseType(session.getCurrentPhase());
                return chatMessageRepository.save(aiMsg);
        }

        private String getAiResponse(InterviewSession session, String userMessage) {
                String systemPrompt = buildSystemPrompt(session);

                List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
                messages.add(SystemMessage.from(systemPrompt));

                List<ChatMessage> history = chatMessageRepository
                                .findBySessionIdOrderByCreatedAtAsc(session.getId());
                for (ChatMessage msg : history) {
                        if (msg.getRole() == Role.USER) {
                                messages.add(UserMessage.from(msg.getContent()));
                        } else {
                                messages.add(AiMessage.from(msg.getContent()));
                        }
                }

                messages.add(UserMessage.from(userMessage));

                ChatRequest chatRequest = ChatRequest.builder()
                                .messages(messages)
                                .build();
                try {
                        var response = chatLanguageModel.chat(chatRequest);
                        return response.aiMessage().text();
                } catch (Exception e) {
                        log.error("AI chat failed: {}", e.getMessage());
                        return " [MOCK MODE] I'm sorry, I'm having trouble connecting to my brain right now (AI API Error). Let's continue the interview assuming you gave a great answer! Can you elaborate more on that or should we move to the next question?";
                }
        }

        private String buildSystemPrompt(InterviewSession session) {
                String resumeJson = resumeRepository.findByUserId(session.getUser().getId())
                                .map(r -> r.getParsedJson() != null ? r.getParsedJson() : r.getRawText())
                                .orElse("{}");

                String company = session.getCompany();
                String difficulty = session.getDifficulty() != null ? session.getDifficulty().name() : "UNKNOWN";
                PhaseType phase = session.getCurrentPhase();
                int duration = getDurationForPhase(session, phase);

                // Replace placeholders in base context
                String baseContext = replacePlaceholders(baseContextTemplate, Map.of(
                                "company", company,
                                "difficulty", difficulty,
                                "phase", phase.name(),
                                "duration", String.valueOf(duration)));

                // Replace placeholders in phase-specific template
                String phasePrompt = switch (phase) {
                        case RESUME_SCREEN -> replacePlaceholders(resumeScreenTemplate, Map.of(
                                        "company", company,
                                        "difficulty", difficulty,
                                        "resumeJson", resumeJson));
                        case DSA -> {
                                String problemInfo = "";
                                if (session.getReportJson() != null && !session.getReportJson().isEmpty()) {
                                        try {
                                                DsaProblem problem = objectMapper.readValue(session.getReportJson(),
                                                                DsaProblem.class);
                                                problemInfo = "\n=== CURRENT PROBLEM ===\nYou are conducting an interview for this specific problem:\n"
                                                                + "Title: " + problem.getTitle() + "\n"
                                                                + "Description: " + problem.getDescription() + "\n"
                                                                + "Constraints: "
                                                                + String.join(", ", problem.getConstraints());
                                        } catch (Exception e) {
                                                log.warn("Failed to parse DsaProblem for chat context", e);
                                        }
                                }
                                yield replacePlaceholders(dsaTemplate, Map.of(
                                                "company", company,
                                                "difficulty", difficulty)) + problemInfo
                                                + buildSubmissionContext(session);
                        }
                        case SYSTEM_DESIGN -> replacePlaceholders(systemDesignTemplate, Map.of(
                                        "company", company,
                                        "difficulty", difficulty));
                        case HR -> replacePlaceholders(hrTemplate, Map.of(
                                        "company", company,
                                        "difficulty", difficulty,
                                        "resumeJson", resumeJson));
                };

                return baseContext + "\n" + phasePrompt;
        }

        private String buildSubmissionContext(InterviewSession session) {
                List<CodeSubmission> submissions = codeSubmissionRepository
                                .findBySessionIdOrderBySubmittedAtDesc(session.getId());

                if (submissions.isEmpty()) {
                        return "\n=== SUBMISSION STATUS ===\nUser has NOT submitted any code yet.";
                }

                CodeSubmission latest = submissions.get(0);
                boolean isSubmitted = latest.getSubmitted() != null && latest.getSubmitted();

                StringBuilder context = new StringBuilder("\n=== LATEST CODE ACTIVITY ===\n");
                context.append("State: ").append(isSubmitted ? "OFFICIALLY SUBMITTED" : "RUNNING/TESTING (Draft)")
                                .append("\n");
                context.append("Language: ").append(latest.getLanguage()).append("\n");
                context.append("Current Code:\n```").append(latest.getLanguage()).append("\n").append(latest.getCode())
                                .append("\n```\n");

                if (latest.getTestResultsJson() != null) {
                        context.append("Latest Execution/Test Results: ").append(latest.getTestResultsJson())
                                        .append("\n");
                }

                if (isSubmitted && latest.getEvaluationJson() != null) {
                        context.append("AI Scoring/Evaluation: ").append(latest.getEvaluationJson()).append("\n");
                }

                if (!isSubmitted) {
                        context.append("\nINSTRUCTION: The user is still working on the code. If their latest run failed tests, you can offer gentle hints or ask about their approach. If it passed, encourage them to submit.");
                } else {
                        context.append("\nINSTRUCTION: The user HAS submitted. Acknowledge the submission and discuss the results/complexity. Do NOT ask them for the code again.");
                }

                return context.toString();
        }

        private String replacePlaceholders(String template, Map<String, String> variables) {
                String result = template;
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                        result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
                }
                return result;
        }

        private int getDurationForPhase(InterviewSession session, PhaseType phase) {
                return switch (phase) {
                        case RESUME_SCREEN -> session.getResumeDurationMins();
                        case DSA -> session.getDsaDurationMins();
                        case SYSTEM_DESIGN -> session.getSystemDesignDurationMins();
                        case HR -> session.getHrDurationMins();
                };
        }
}
