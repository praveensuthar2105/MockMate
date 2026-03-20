package com.mockmate.service;

import com.mockmate.model.InterviewSession;
import com.mockmate.model.PhaseType;
import com.mockmate.model.Resume;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PhaseQuestionService {

    private final ChatLanguageModel chatLanguageModel;
    private final ResumeRepository resumeRepository;

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
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load prompt template: {}", path, e);
            throw new RuntimeException("Failed to load prompt template: " + path, e);
        }
    }

    public String generateFirstQuestion(InterviewSession session) {
        try {
            return switch (session.getCurrentPhase()) {
                case RESUME_SCREEN -> generateResumeScreenOpener(session);
                case DSA -> generateDsaOpener(session);
                case SYSTEM_DESIGN -> generateSystemDesignOpener(session);
                case HR -> generateHrOpener(session);
            };
        } catch (Exception e) {
            log.error("Failed to generate opening question via Gemini. Falling back to default.", e);
            return getFallbackOpener(session.getCurrentPhase());
        }
    }

    private String generateResumeScreenOpener(InterviewSession session) {
        String systemPrompt = buildSystemPrompt(session, resumeScreenTemplate);
        return callGemini(systemPrompt,
                "Please generate the opening question for the resume screening round based on my resume.");
    }

    private String generateDsaOpener(InterviewSession session) {
        String systemPrompt = buildSystemPrompt(session, dsaTemplate);
        return callGemini(systemPrompt, "Please generate the opening DSA problem for this interview.");
    }

    private String generateSystemDesignOpener(InterviewSession session) {
        String systemPrompt = buildSystemPrompt(session, systemDesignTemplate);
        return callGemini(systemPrompt, "Please generate the opening System Design prompt for this interview.");
    }

    private String generateHrOpener(InterviewSession session) {
        String systemPrompt = buildSystemPrompt(session, hrTemplate);
        return callGemini(systemPrompt, "Please generate the first behavioral question for this HR round.");
    }

    private String callGemini(String systemPrompt, String userPrompt) {
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(List.of(
                        SystemMessage.from(systemPrompt),
                        dev.langchain4j.data.message.UserMessage.from(userPrompt)))
                .build();
        return chatLanguageModel.chat(chatRequest).aiMessage().text();
    }

    private String buildSystemPrompt(InterviewSession session, String phaseTemplate) {
        String resumeJson = resumeRepository.findByUserId(session.getUser().getId())
                .map(r -> r.getParsedJson() != null ? r.getParsedJson() : r.getRawText())
                .orElse("{}");

        String company = session.getCompany();
        String difficulty = session.getDifficulty() != null ? session.getDifficulty().name() : "UNKNOWN";
        PhaseType phase = session.getCurrentPhase();
        int duration = getDurationForPhase(session, phase);

        String baseContext = replacePlaceholders(baseContextTemplate, Map.of(
                "company", company,
                "difficulty", difficulty,
                "phase", phase.name(),
                "duration", String.valueOf(duration)));

        String phasePrompt = replacePlaceholders(phaseTemplate, Map.of(
                "company", company,
                "difficulty", difficulty,
                "resumeJson", resumeJson));

        return baseContext + "\n" + phasePrompt;
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
