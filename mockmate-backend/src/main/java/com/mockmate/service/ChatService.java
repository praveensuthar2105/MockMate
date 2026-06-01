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
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

        private final Optional<ChatLanguageModel> chatLanguageModel;
        private final ChatMessageRepository chatMessageRepository;
        private final InterviewSessionRepository sessionRepository;
        private final ResumeRepository resumeRepository;
        private final CodeSubmissionRepository codeSubmissionRepository;
        private final ObjectMapper objectMapper;
        private final SimpMessagingTemplate simpMessagingTemplate;

        private String baseContextTemplate;
        private String resumeScreenTemplate;
        private String dsaTemplate;
        private String systemDesignTemplate;
        private String hrTemplate;
        private String dsaAnalysisTemplate;
        private String dsaFollowupEvalTemplate;

        @PostConstruct
        public void loadPromptTemplates() {
                baseContextTemplate = loadTemplate("prompts/base-context.txt");
                resumeScreenTemplate = loadTemplate("prompts/resume-screen.txt");
                dsaTemplate = loadTemplate("prompts/dsa.txt");
                systemDesignTemplate = loadTemplate("prompts/system-design.txt");
                hrTemplate = loadTemplate("prompts/hr.txt");
                dsaAnalysisTemplate = loadTemplate("prompts/dsa-analysis.txt");
                dsaFollowupEvalTemplate = loadTemplate("prompts/dsa-followup-eval.txt");
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
                InterviewSession session = sessionRepository.findByIdWithUser(sessionId)
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
                if (chatLanguageModel.isEmpty()) {
                        log.warn("Gemini API key is not configured. Running chat in fallback mode.");
                        return "[MOCK MODE] AI is not configured yet. I can continue the interview with fallback responses. Tell me your approach, and I’ll guide the next step.";
                }

                if (session.getCurrentPhase() == PhaseType.DSA) {
                        try {
                                String problemJson = session.getDsaProblemJson();
                                if (problemJson != null && !problemJson.isBlank()) {
                                        DsaProblem problem = objectMapper.readValue(problemJson, DsaProblem.class);

                                        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
                                        String lastAiMsg = "Please explain your approach and how you plan to solve the problem.";
                                        for (int i = history.size() - 1; i >= 0; i--) {
                                                if (history.get(i).getRole() == Role.AI) {
                                                        lastAiMsg = history.get(i).getContent();
                                                        break;
                                                }
                                        }

                                        List<CodeSubmission> submissions = codeSubmissionRepository.findBySessionIdOrderBySubmittedAtDesc(session.getId());
                                        String code = "No code written yet.";
                                        String language = "Java";
                                        String runStatus = "Not run yet.";
                                        if (!submissions.isEmpty()) {
                                                CodeSubmission latest = submissions.get(0);
                                                code = latest.getCode() != null ? latest.getCode() : "";
                                                language = latest.getLanguage() != null ? latest.getLanguage() : "Java";
                                                if (latest.getTestResultsJson() != null) {
                                                        runStatus = "Tests run completed.";
                                                }
                                        }

                                        String prompt = dsaFollowupEvalTemplate
                                                        .replace("{problemTitle}", problem.getTitle())
                                                        .replace("{language}", language)
                                                        .replace("{code}", code)
                                                        .replace("{runStatus}", runStatus)
                                                        .replace("{followUpQuestion}", lastAiMsg)
                                                        .replace("{candidateAnswer}", userMessage);

                                        ChatRequest chatRequest = ChatRequest.builder()
                                                        .messages(List.of(UserMessage.from(prompt)))
                                                        .build();

                                        String response = chatLanguageModel.get().chat(chatRequest).aiMessage().text();
                                        String cleanJson = extractJson(response);

                                        DsaFollowupEvalResponse eval = objectMapper.readValue(cleanJson, DsaFollowupEvalResponse.class);

                                        String reply = "";
                                        if (eval.getEvaluation() != null && !eval.getEvaluation().equalsIgnoreCase("null")) {
                                                reply += eval.getEvaluation() + " ";
                                        }
                                        if (eval.getNextFollowUp() != null && !eval.getNextFollowUp().equalsIgnoreCase("null")) {
                                                reply += eval.getNextFollowUp();
                                        }

                                        reply = reply.trim();
                                        if (!reply.isEmpty()) {
                                                return reply;
                                        }
                                }
                        } catch (Exception e) {
                                log.warn("Failed structured DSA follow-up evaluation, falling back to standard prompt.", e);
                        }
                }

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
                        var response = chatLanguageModel.orElseThrow().chat(chatRequest);
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
                                "duration", String.valueOf(duration),
                "candidateName", session.getUser() != null ? String.valueOf(session.getUser().getId()) : "Candidate"));

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
                        case SYSTEM_DESIGN -> 0;
                        case HR -> session.getHrDurationMins();
                };
        }

        public void triggerAsyncCodeFeedback(Long sessionId, String code, String language, com.mockmate.dto.code.ExecutionResult runResult) {
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                                Thread.sleep(1500); // Wait for the frontend to render compile/test result first

                                InterviewSession session = sessionRepository.findByIdWithUser(sessionId)
                                                .orElseThrow(() -> new RuntimeException("Interview session not found"));

                                if (session.getStatus() == SessionStatus.COMPLETED || chatLanguageModel.isEmpty()) {
                                        return;
                                }

                                String problemJson = session.getDsaProblemJson();
                                if (problemJson == null || problemJson.isBlank()) {
                                        return;
                                }
                                DsaProblem problem = objectMapper.readValue(problemJson, DsaProblem.class);

                                long elapsed = 0;
                                if (session.getStartedAt() != null) {
                                        elapsed = java.time.Duration.between(session.getStartedAt(), java.time.LocalDateTime.now()).toMinutes();
                                }

                                String testCaseDetails = "Test cases detail:\n";
                                if (runResult.getResults() != null && !runResult.getResults().isEmpty()) {
                                        for (int i = 0; i < runResult.getResults().size(); i++) {
                                                var r = runResult.getResults().get(i);
                                                testCaseDetails += "Test Case " + (i + 1) + ": Input: " + r.getInput() + " | Expected: " + r.getExpectedOutput() + " | Actual: " + r.getActualOutput() + " | Status: " + r.getStatus() + "\n";
                                        }
                                } else {
                                        testCaseDetails += runResult.getCompileError() != null ? runResult.getCompileError() : "None";
                                }

                                String prompt = dsaAnalysisTemplate
                                                .replace("{problemTitle}", problem.getTitle())
                                                .replace("{problemDescription}", problem.getDescription())
                                                .replace("{difficulty}", session.getDifficulty() != null ? session.getDifficulty().name() : "MEDIUM")
                                                .replace("{language}", language)
                                                .replace("{timeElapsed}", String.valueOf(elapsed))
                                                .replace("{code}", code)
                                                .replace("{runStatus}", runResult.isCompiled() ? (runResult.isAllPassed() ? "PASSED" : "FAILED") : "COMPILATION_ERROR")
                                                .replace("{testCaseDetails}", testCaseDetails);

                                ChatRequest chatRequest = ChatRequest.builder()
                                                .messages(List.of(UserMessage.from(prompt)))
                                                .build();
                                String response = chatLanguageModel.get().chat(chatRequest).aiMessage().text();
                                String cleanJson = extractJson(response);

                                DsaAnalysisResponse analysis = objectMapper.readValue(cleanJson, DsaAnalysisResponse.class);

                                // Update complexity if detected
                                if (analysis.getComplexity() != null) {
                                        String time = analysis.getComplexity().getTime();
                                        String space = analysis.getComplexity().getSpace();
                                        List<CodeSubmission> submissions = codeSubmissionRepository
                                                        .findBySessionIdOrderBySubmittedAtDesc(session.getId());
                                        if (!submissions.isEmpty()) {
                                                CodeSubmission latest = submissions.get(0);
                                                if (time != null && !time.equalsIgnoreCase("null") && !time.isBlank()) {
                                                        latest.setTimeComplexity(time);
                                                }
                                                if (space != null && !space.equalsIgnoreCase("null") && !space.isBlank()) {
                                                        latest.setSpaceComplexity(space);
                                                }
                                                codeSubmissionRepository.save(latest);
                                        }
                                }

                                String aiResponseText = "";
                                if (analysis.getFeedback() != null && !analysis.getFeedback().isBlank()) {
                                        aiResponseText += analysis.getFeedback() + " ";
                                }
                                if (analysis.getHint() != null && !analysis.getHint().isBlank() && !analysis.getHint().equalsIgnoreCase("null")) {
                                        aiResponseText += analysis.getHint() + " ";
                                }
                                if (analysis.getFollowUp() != null && !analysis.getFollowUp().isBlank() && !analysis.getFollowUp().equalsIgnoreCase("null")) {
                                        aiResponseText += analysis.getFollowUp();
                                }
                                aiResponseText = aiResponseText.trim();

                                if (aiResponseText.isEmpty()) {
                                        return;
                                }

                                ChatMessage aiMsg = new ChatMessage();
                                aiMsg.setSession(session);
                                aiMsg.setRole(Role.AI);
                                aiMsg.setContent(aiResponseText);
                                aiMsg.setPhaseType(session.getCurrentPhase());
                                chatMessageRepository.save(aiMsg);

                                simpMessagingTemplate.convertAndSend(
                                                "/topic/session/" + sessionId,
                                                com.mockmate.dto.ws.WsEvent.message(aiResponseText));

                                log.info("Live code run feedback broadcasted for session {}", sessionId);

                        } catch (Exception e) {
                                log.error("Failed to generate live code feedback", e);
                        }
                });
        }

        public void triggerAsyncCodeDraftFeedback(Long sessionId, String code, String language) {
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                                List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
                                if (!history.isEmpty()) {
                                        ChatMessage lastMsg = history.get(history.size() - 1);
                                        if (lastMsg.getRole() == Role.AI && lastMsg.getCreatedAt() != null) {
                                                java.time.Duration age = java.time.Duration.between(lastMsg.getCreatedAt(), java.time.LocalDateTime.now());
                                                if (age.getSeconds() < 60) {
                                                        return;
                                                }
                                        }
                                }

                                InterviewSession session = sessionRepository.findByIdWithUser(sessionId)
                                                .orElseThrow(() -> new RuntimeException("Interview session not found"));

                                if (session.getStatus() == SessionStatus.COMPLETED || chatLanguageModel.isEmpty()) {
                                        return;
                                }

                                String problemJson = session.getDsaProblemJson();
                                if (problemJson == null || problemJson.isBlank()) {
                                        return;
                                }
                                DsaProblem problem = objectMapper.readValue(problemJson, DsaProblem.class);

                                long elapsed = 0;
                                if (session.getStartedAt() != null) {
                                        elapsed = java.time.Duration.between(session.getStartedAt(), java.time.LocalDateTime.now()).toMinutes();
                                }

                                String prompt = dsaAnalysisTemplate
                                                .replace("{problemTitle}", problem.getTitle())
                                                .replace("{problemDescription}", problem.getDescription())
                                                .replace("{difficulty}", session.getDifficulty() != null ? session.getDifficulty().name() : "MEDIUM")
                                                .replace("{language}", language)
                                                .replace("{timeElapsed}", String.valueOf(elapsed))
                                                .replace("{code}", code)
                                                .replace("{runStatus}", "In Progress (Draft)")
                                                .replace("{testCaseDetails}", "No execution results available yet. The candidate is actively editing code.");

                                ChatRequest chatRequest = ChatRequest.builder()
                                                .messages(List.of(UserMessage.from(prompt)))
                                                .build();
                                String response = chatLanguageModel.get().chat(chatRequest).aiMessage().text();
                                String cleanJson = extractJson(response);

                                DsaAnalysisResponse analysis = objectMapper.readValue(cleanJson, DsaAnalysisResponse.class);

                                if (analysis.getSeverity() != null && analysis.getSeverity().equalsIgnoreCase("none")) {
                                        return;
                                }

                                String aiResponseText = "";
                                if (analysis.getFeedback() != null && !analysis.getFeedback().isBlank()) {
                                        aiResponseText += analysis.getFeedback() + " ";
                                }
                                if (analysis.getHint() != null && !analysis.getHint().isBlank() && !analysis.getHint().equalsIgnoreCase("null")) {
                                        aiResponseText += analysis.getHint() + " ";
                                }
                                if (analysis.getFollowUp() != null && !analysis.getFollowUp().isBlank() && !analysis.getFollowUp().equalsIgnoreCase("null")) {
                                        aiResponseText += analysis.getFollowUp();
                                }
                                aiResponseText = aiResponseText.trim();

                                if (aiResponseText.isEmpty()) {
                                        return;
                                }

                                ChatMessage aiMsg = new ChatMessage();
                                aiMsg.setSession(session);
                                aiMsg.setRole(Role.AI);
                                aiMsg.setContent(aiResponseText);
                                aiMsg.setPhaseType(session.getCurrentPhase());
                                chatMessageRepository.save(aiMsg);

                                simpMessagingTemplate.convertAndSend(
                                                "/topic/session/" + sessionId,
                                                com.mockmate.dto.ws.WsEvent.message(aiResponseText));

                                log.info("Live code draft feedback broadcasted for session {}", sessionId);

                        } catch (Exception e) {
                                log.error("Failed to generate live code draft feedback", e);
                        }
                });
        }

        @Transactional
        public void persistDraftSubmission(Long sessionId, String language, String code) {
                try {
                        InterviewSession session = sessionRepository.findByIdWithUser(sessionId)
                                        .orElseThrow(() -> new RuntimeException("Interview session not found"));

                        List<CodeSubmission> submissions = codeSubmissionRepository
                                        .findBySessionIdOrderBySubmittedAtDesc(sessionId);

                        CodeSubmission submission;
                        if (!submissions.isEmpty()
                                        && (submissions.get(0).getSubmitted() == null || !submissions.get(0).getSubmitted())) {
                                submission = submissions.get(0);
                        } else {
                                submission = new CodeSubmission();
                                submission.setSession(session);
                                submission.setHintsUsed(0);
                        }

                        submission.setLanguage(language);
                        submission.setCode(code);
                        submission.setSubmitted(false);
                        submission.setSubmittedAt(java.time.LocalDateTime.now());

                        codeSubmissionRepository.save(submission);
                } catch (Exception e) {
                        log.error("Failed to persist draft submission", e);
                }
        }

        private String extractJson(String text) {
                if (text == null)
                        return null;

                int firstBrace = text.indexOf('{');
                int lastBrace = text.lastIndexOf('}');

                if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                        return text.substring(firstBrace, lastBrace + 1);
                }

                return text.replaceAll("```json", "").replaceAll("```", "").trim();
        }

        @lombok.Data
        @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
        public static class DsaAnalysisResponse {
                private String feedback;
                private String hint;
                private String followUp;
                private Complexity complexity;
                private String severity;

                @lombok.Data
                @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
                public static class Complexity {
                        private String time;
                        private String space;
                }
        }

        @lombok.Data
        @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
        public static class DsaFollowupEvalResponse {
                private String evaluation;
                private String nextFollowUp;
                private boolean shouldProceed;
        }
}

