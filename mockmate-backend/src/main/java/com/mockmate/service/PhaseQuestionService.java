package com.mockmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockmate.dto.code.DsaProblem;
import com.mockmate.model.InterviewSession;
import com.mockmate.model.PhaseType;
import com.mockmate.repository.ResumeRepository;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhaseQuestionService {

    private final Optional<ChatLanguageModel> chatLanguageModel;
    private final ResumeRepository resumeRepository;
    private final com.mockmate.repository.InterviewSessionRepository sessionRepository;
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
    }

    private String loadTemplate(String path) {
        try {
            return new ClassPathResource(path).getContentAsString(Objects.requireNonNull(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Failed to load prompt template: {}", path, e);
            throw new RuntimeException("Failed to load prompt template: " + path, e);
        }
    }

    public String generateFirstQuestion(InterviewSession session) {
        return generateFirstQuestionForPhase(session, session.getCurrentPhase());
    }

    public String generateFirstQuestionForPhase(InterviewSession session, PhaseType phase) {
        try {
            return switch (phase) {
                case RESUME_SCREEN -> generateResumeScreenOpenerForPhase(session, phase);
                case DSA -> generateDsaOpenerForPhase(session, phase);
                case SYSTEM_DESIGN -> generateSystemDesignOpenerForPhase(session, phase);
                case HR -> generateHrOpenerForPhase(session, phase);
            };
        } catch (Exception e) {
            log.error("Failed to generate opening question for phase {} via Gemini. Falling back to default.", phase, e);
            return getFallbackOpener(phase);
        }
    }

    private String generateResumeScreenOpenerForPhase(InterviewSession session, PhaseType phase) {
        String systemPrompt = buildSystemPrompt(session, phase, resumeScreenTemplate);
        return callGemini(systemPrompt,
                "Please generate the opening question for the resume screening round based on my resume.");
    }

    private String generateDsaOpenerForPhase(InterviewSession session, PhaseType phase) {
        String problemInfo = "a general coding problem";
        if (session.getDsaProblemJson() != null && !session.getDsaProblemJson().isEmpty()) {
            try {
                DsaProblem problem = objectMapper.readValue(session.getDsaProblemJson(), DsaProblem.class);
                problemInfo = "the following problem: '" + problem.getTitle() + "'\nDescription: "
                        + problem.getDescription();
            } catch (Exception e) {
                log.warn("Failed to parse DsaProblem for opener", e);
            }
        }
        String systemPrompt = buildSystemPrompt(session, phase, dsaTemplate);
        return callGemini(systemPrompt, "Please introduce this coding round and present " + problemInfo);
    }

    private String generateSystemDesignOpenerForPhase(InterviewSession session, PhaseType phase) {
        String systemPrompt = buildSystemPrompt(session, phase, systemDesignTemplate);
        return callGemini(systemPrompt, "Please generate the opening System Design prompt for this interview.");
    }

    private String generateHrOpenerForPhase(InterviewSession session, PhaseType phase) {
        String systemPrompt = buildSystemPrompt(session, phase, hrTemplate);
        return callGemini(systemPrompt, "Please generate the first behavioral question for this HR round.");
    }

    private String callGemini(String systemPrompt, String userPrompt) {
        if (chatLanguageModel.isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }

        ChatRequest chatRequest = ChatRequest.builder()
                .messages(List.of(
                        SystemMessage.from(systemPrompt),
                        dev.langchain4j.data.message.UserMessage.from(userPrompt)))
                .build();
        return chatLanguageModel.orElseThrow().chat(chatRequest).aiMessage().text();
    }

    private String buildSystemPrompt(InterviewSession session, PhaseType phase, String phaseTemplate) {
        String resumeJson = resumeRepository.findByUserId(session.getUser().getId())
                .map(r -> r.getParsedJson() != null ? r.getParsedJson() : r.getRawText())
                .orElse("{}");

        String company = session.getCompany();
        String difficulty = session.getDifficulty() != null ? session.getDifficulty().name() : "UNKNOWN";
        int duration = getDurationForPhase(session, phase);

        String baseContext = replacePlaceholders(baseContextTemplate, Map.of(
                "company", company,
                "difficulty", difficulty,
                "phase", phase.name(),
                "candidateName", session.getUser().getName() != null ? session.getUser().getName() : "Candidate",
                "duration", String.valueOf(duration)));

        String phasePrompt = replacePlaceholders(phaseTemplate, Map.of(
                "company", company,
                "difficulty", difficulty,
                "resumeJson", resumeJson));

        String historyContext = buildHistoryContext(session.getUser().getId());

        return baseContext + "\n" + historyContext + "\n" + phasePrompt;
    }

    private String buildHistoryContext(Long userId) {
        List<InterviewSession> sessions = sessionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .filter(s -> s.getStatus() == com.mockmate.model.SessionStatus.COMPLETED && s.getTotalScore() != null)
                .collect(java.util.stream.Collectors.toList());

        if (sessions.isEmpty()) {
            return "\n=== CANDIDATE LONG-TERM CONTEXT ===\nNo historical interviews completed. This is the candidate's first session.";
        }

        double avg = sessions.stream().mapToInt(InterviewSession::getTotalScore).average().orElse(0);
        int max = sessions.stream().mapToInt(InterviewSession::getTotalScore).max().orElse(0);

        StringBuilder history = new StringBuilder("\n=== CANDIDATE LONG-TERM CONTEXT ===\n");
        history.append("Total mock interviews completed: ").append(sessions.size()).append("\n");
        history.append("Average Score: ").append(String.format("%.1f", avg)).append("/100\n");
        history.append("Highest Score: ").append(max).append("/100\n");

        // Extract key feedback/improvement areas from the latest completed session
        InterviewSession lastSession = sessions.get(0);
        if (lastSession.getReportJson() != null) {
            try {
                com.fasterxml.jackson.databind.JsonNode report = objectMapper.readTree(lastSession.getReportJson());
                java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = report.fields();
                StringBuilder weaknessesBuilder = new StringBuilder();
                StringBuilder strengthsBuilder = new StringBuilder();
                
                while (fields.hasNext()) {
                    java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> field = fields.next();
                    com.fasterxml.jackson.databind.JsonNode phaseEval = field.getValue();
                    
                    com.fasterxml.jackson.databind.JsonNode weaknesses = phaseEval.path("weaknesses");
                    if (weaknesses.isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode node : weaknesses) {
                            weaknessesBuilder.append(" - [").append(field.getKey()).append("] ").append(node.asText()).append("\n");
                        }
                    }
                    
                    com.fasterxml.jackson.databind.JsonNode strengths = phaseEval.path("strengths");
                    if (strengths.isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode node : strengths) {
                            strengthsBuilder.append(" - [").append(field.getKey()).append("] ").append(node.asText()).append("\n");
                        }
                    }
                }
                
                if (strengthsBuilder.length() > 0) {
                    history.append("Past Strengths:\n").append(strengthsBuilder);
                }
                if (weaknessesBuilder.length() > 0) {
                    history.append("Past Areas to Improve (Weaknesses):\n").append(weaknessesBuilder);
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        history.append("\nINTERVIEWER INSTRUCTION: Use this historical performance context to guide your evaluation. Focus on testing if the candidate has addressed their past areas to improve (especially weaknesses in code, structure, or communication) without explicitly saying 'In your last interview...'. Challenge them constructively to show growth.");
        return history.toString();
    }

    private String replacePlaceholders(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    private String getFallbackOpener(PhaseType phase) {
        return switch (phase) {
            case RESUME_SCREEN ->
                "Hi, thanks for joining today. Can you start by introducing yourself and walking me through your most recent project?";
            case DSA ->
                "Let's move to the coding round. I'll give you a problem — take your time to think it through before jumping to code.";
            case SYSTEM_DESIGN ->
                "Now for system design. Walk me through how you would design a URL shortener from scratch.";
            case HR ->
                "Last section — just a few behavioral questions. Can you tell me about a time you worked through a challenging situation in a team?";
        };
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
