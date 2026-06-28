package com.mockmate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mockmate.dto.response.VoiceSessionTokenResponse;
import com.mockmate.dto.response.ChatResponse;
import com.mockmate.dto.ws.WsEvent;
import com.mockmate.model.ChatMessage;
import com.mockmate.model.InterviewSession;
import com.mockmate.model.Role;
import com.mockmate.model.SessionStatus;
import com.mockmate.repository.ChatMessageRepository;
import com.mockmate.repository.InterviewSessionRepository;
import com.mockmate.repository.ResumeRepository;
import com.mockmate.repository.CodeSubmissionRepository;
import com.mockmate.model.CodeSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.List;

@Service
public class VoiceSessionService {

    private static final String AUTH_TOKEN_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1alpha/auth_tokens";

    private final InterviewSessionRepository sessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ResumeRepository resumeRepository;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final RestClient.Builder restClientBuilder;
    private final InterviewService interviewService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate simpMessagingTemplate;

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.gemini.live-model:gemini-3.1-flash-live-preview}")
    private String liveModel;

    public VoiceSessionService(
            InterviewSessionRepository sessionRepository,
            ChatMessageRepository chatMessageRepository,
            ResumeRepository resumeRepository,
            CodeSubmissionRepository codeSubmissionRepository,
            RestClient.Builder restClientBuilder,
            @org.springframework.context.annotation.Lazy InterviewService interviewService,
            org.springframework.messaging.simp.SimpMessagingTemplate simpMessagingTemplate) {
        this.sessionRepository = sessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.resumeRepository = resumeRepository;
        this.codeSubmissionRepository = codeSubmissionRepository;
        this.restClientBuilder = restClientBuilder;
        this.interviewService = interviewService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public VoiceSessionTokenResponse createToken(Long sessionId, String username) {
        InterviewSession session = sessionRepository.findByIdWithUser(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Interview session not found"));

        if (!session.getUser().getEmail().equals(username)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Interview session does not belong to this user");
        }
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Interview session is not active");
        }
        if (!StringUtils.hasText(geminiApiKey)) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Real-time voice is not configured");
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plus(30, ChronoUnit.MINUTES);
        Instant newSessionExpiresAt = now.plus(1, ChronoUnit.MINUTES);

        String systemInstruction = buildSystemInstruction(session);

        Map<String, Object> request = Map.of(
                "uses", 1,
                "expireTime", expiresAt.toString(),
                "newSessionExpireTime", newSessionExpiresAt.toString(),
                "bidiGenerateContentSetup", Map.of(
                        "model", "models/" + liveModel,
                        "generationConfig", Map.of(
                                "responseModalities", new String[]{"AUDIO"},
                                "temperature", 0.7
                        ),
                        "systemInstruction", Map.of(
                                "role", "system",
                                "parts", new Object[]{
                                        Map.of("text", systemInstruction)
                                }
                        ),
                        "sessionResumption", Map.of(),
                        "inputAudioTranscription", Map.of(),
                        "outputAudioTranscription", Map.of(),
                        "tools", new Object[]{
                                Map.of(
                                        "functionDeclarations", new Object[]{
                                                Map.of(
                                                        "name", "readCandidateCode",
                                                        "description", "Retrieve the candidate's current programming code from the technical editor. Call this whenever the candidate mentions their code, refers to their implementation, or when you want to review and discuss their coding approach during the DSA coding round.",
                                                        "parameters", Map.of(
                                                                "type", "OBJECT",
                                                                "properties", Map.of()
                                                        )
                                                )
                                        }
                                )
                        }
                )
        );

        JsonNode response;
        try {
            response = restClientBuilder.build()
                    .post()
                    .uri(AUTH_TOKEN_ENDPOINT)
                    .header("x-goog-api-key", geminiApiKey)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to create a real-time voice session",
                    exception);
        }

        String token = response != null ? response.path("name").asText() : null;
        if (!StringUtils.hasText(token)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Voice provider returned an invalid session token");
        }

        return new VoiceSessionTokenResponse(token, liveModel, expiresAt);
    }

    public ChatResponse saveTranscript(
            Long sessionId,
            String username,
            Role role,
            String content) {
        InterviewSession session = getOwnedActiveSession(sessionId, username);

        String normalizedContent = content == null ? "" : content.trim();
        if (!StringUtils.hasText(normalizedContent)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transcript content is required");
        }

        List<ChatMessage> history =
                chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        if (!history.isEmpty()) {
            ChatMessage last = history.get(history.size() - 1);
            if (last.getRole() == role && normalizedContent.equals(last.getContent())) {
                return toResponse(last);
            }
        }

        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setRole(role);
        message.setContent(normalizedContent);
        message.setPhaseType(session.getCurrentPhase());

        ChatMessage saved = chatMessageRepository.save(message);

        // Auto-advance logic for voice transcripts (matching Chat WebSocket behavior)
        if (session.getStatus() == SessionStatus.IN_PROGRESS) {
            boolean shouldAdvance = false;
            String lowercaseContent = normalizedContent.toLowerCase();
            
            if (role == Role.AI) {
                if (lowercaseContent.contains("move to the next round") ||
                    lowercaseContent.contains("move to the next section") ||
                    lowercaseContent.contains("let's move to the next") ||
                    lowercaseContent.contains("we're out of time for this round") ||
                    lowercaseContent.contains("let's wrap this up and move on")) {
                    shouldAdvance = true;
                }
            } else if (role == Role.USER) {
                if (lowercaseContent.contains("next round") || 
                    lowercaseContent.contains("skip this round") ||
                    lowercaseContent.contains("move to next phase")) {
                    shouldAdvance = true;
                }
            }

            if (shouldAdvance) {
                try {
                    com.mockmate.model.PhaseType nextPhase = interviewService.advancePhase(sessionId);
                    simpMessagingTemplate.convertAndSend(
                            "/topic/session/" + sessionId,
                            WsEvent.phaseChange(nextPhase));
                } catch (Exception e) {
                    // Log error but don't fail transaction
                }
            }
        }

        return toResponse(saved);
    }

    private InterviewSession getOwnedActiveSession(Long sessionId, String username) {
        InterviewSession session = sessionRepository.findByIdWithUser(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Interview session not found"));

        if (!session.getUser().getEmail().equals(username)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Interview session does not belong to this user");
        }
        return session;
    }

    private String buildSystemInstruction(InterviewSession session) {
        StringBuilder instruction = new StringBuilder("""
                You are MockMate, a professional real-time interviewer.
                Conduct the interview verbally with concise, natural responses.
                Ask one question at a time. Do not provide model answers unless the candidate asks for feedback.
                Acknowledge interruptions naturally and continue from the candidate's latest statement.
                """);

        instruction.append("\nCompany: ").append(session.getCompany());
        instruction.append("\nRole: ").append(session.getJobRole());
        instruction.append("\nDifficulty: ").append(session.getDifficulty());
        instruction.append("\nCurrent phase: ").append(session.getCurrentPhase());

        // Append parsed/raw Resume text during Resume Screen and HR rounds
        if (session.getCurrentPhase() == com.mockmate.model.PhaseType.RESUME_SCREEN || session.getCurrentPhase() == com.mockmate.model.PhaseType.HR) {
            String resumeJson = resumeRepository.findByUserId(session.getUser().getId())
                    .map(r -> r.getParsedJson() != null ? r.getParsedJson() : r.getRawText())
                    .orElse("{}");
            instruction.append("\nCandidate Resume Details:\n").append(resumeJson);
        }

        // Append DSA problem details
        if (StringUtils.hasText(session.getDsaProblemJson())) {
            instruction.append("\nCurrent DSA problem JSON: ").append(session.getDsaProblemJson());
        }

        // Append active Code Draft/Submission during DSA coding round
        if (session.getCurrentPhase() == com.mockmate.model.PhaseType.DSA) {
            instruction.append(buildSubmissionContext(session));
        }

        // Append historical candidate progress context
        String historyContext = buildHistoryContext(session.getUser().getId());
        instruction.append("\n").append(historyContext);

        List<ChatMessage> history =
                chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        if (!history.isEmpty()) {
            instruction.append("\nConversation so far:");
            int start = Math.max(0, history.size() - 12);
            for (int index = start; index < history.size(); index++) {
                ChatMessage message = history.get(index);
                instruction.append("\n")
                        .append(message.getRole())
                        .append(": ")
                        .append(message.getContent());
            }
        }

        return instruction.toString();
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
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode report = mapper.readTree(lastSession.getReportJson());
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

    private ChatResponse toResponse(ChatMessage message) {
        return ChatResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .phaseType(message.getPhaseType())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
