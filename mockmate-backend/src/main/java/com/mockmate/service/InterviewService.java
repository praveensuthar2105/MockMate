package com.mockmate.service;

import com.mockmate.dto.request.InterviewRequest;
import com.mockmate.dto.response.InterviewResponse;
import com.mockmate.model.*;
import com.mockmate.repository.InterviewSessionRepository;
import com.mockmate.repository.PhaseResultRepository;
import com.mockmate.repository.UserRepository;
import com.mockmate.repository.ChatMessageRepository;
import com.mockmate.dto.response.ChatMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final PhaseResultRepository phaseResultRepository;
    private final UserRepository userRepository;
    private final PhaseTimerService phaseTimerService;
    private final PhaseQuestionService phaseQuestionService;
    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;
    private final ScoringService scoringService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final DsaProblemService dsaProblemService;

    public InterviewService(InterviewSessionRepository sessionRepository,
            PhaseResultRepository phaseResultRepository,
            UserRepository userRepository,
            @Lazy PhaseTimerService phaseTimerService,
            PhaseQuestionService phaseQuestionService,
            ChatService chatService,
            ChatMessageRepository chatMessageRepository,
            ScoringService scoringService,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
            @Lazy DsaProblemService dsaProblemService) {
        this.sessionRepository = sessionRepository;
        this.phaseResultRepository = phaseResultRepository;
        this.userRepository = userRepository;
        this.phaseTimerService = phaseTimerService;
        this.phaseQuestionService = phaseQuestionService;
        this.chatService = chatService;
        this.chatMessageRepository = chatMessageRepository;
        this.scoringService = scoringService;
        this.objectMapper = objectMapper;
        this.dsaProblemService = dsaProblemService;
    }

    @Transactional
    public InterviewResponse createSession(Long userId, InterviewRequest request) {
        Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<InterviewSession> activeSessions = sessionRepository.findByUserIdAndStatus(userId, SessionStatus.IN_PROGRESS);
        for (InterviewSession active : activeSessions) {
            active.setStatus(SessionStatus.COMPLETED);
            active.setEndedAt(LocalDateTime.now());
            sessionRepository.save(active);
            log.info("Automatically completed previous active session {} for user {}", active.getId(), userId);
        }

        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setCompany(request.getCompany());
        session.setJobRole(request.getJobRole());
        session.setDifficulty(Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        session.setInterviewType(request.getType() != null ? request.getType() : InterviewType.FULL_MOCK);
        session.setStatus(SessionStatus.IN_PROGRESS);

        PhaseType initialPhase = PhaseType.RESUME_SCREEN;
        if (request.getSelectedPhases() != null && !request.getSelectedPhases().isEmpty()) {
            try {
                initialPhase = PhaseType.valueOf(request.getSelectedPhases().get(0).toUpperCase());
                session.setSelectedPhases(String.join(",", request.getSelectedPhases()));
            } catch (IllegalArgumentException e) {
                // fallback
                session.setSelectedPhases("RESUME_SCREEN,DSA,HR");
            }
        } else {
            initialPhase = switch (session.getInterviewType()) {
                case DSA_ONLY -> {
                    session.setSelectedPhases("DSA");
                    yield PhaseType.DSA;
                }
                case HR_ONLY -> {
                    session.setSelectedPhases("HR");
                    yield PhaseType.HR;
                }
                default -> {
                    session.setSelectedPhases("RESUME_SCREEN,DSA,HR");
                    yield PhaseType.RESUME_SCREEN;
                }
            };
        }
        session.setCurrentPhase(initialPhase);

        session.setResumeDurationMins(Objects.requireNonNullElse(request.getResumeDurationMins(), 5));
        session.setDsaDurationMins(Objects.requireNonNullElse(request.getDsaDurationMins(), 30));
        session.setHrDurationMins(Objects.requireNonNullElse(request.getHrDurationMins(), 10));

        InterviewSession saved = sessionRepository.save(session);
        log.info("Interview session created: id={}, user={}, company={}", saved.getId(), userId, request.getCompany());

        return mapToResponse(saved);
    }

    @Transactional
    public InterviewResponse startSession(Long sessionId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));

        session.setStartedAt(LocalDateTime.now());

        int duration = switch (session.getCurrentPhase()) {
            case RESUME_SCREEN -> session.getResumeDurationMins();
            case DSA -> session.getDsaDurationMins();
            case HR -> session.getHrDurationMins();
            default -> 10;
        };
        session.setPhaseEndTime(LocalDateTime.now().plusMinutes(duration));

        InterviewSession saved = sessionRepository.save(session);
        log.info("Interview session started: id={}", saved.getId());

        // Pre-generate problem if starting in DSA phase so AI interviewer knows it
        if (saved.getCurrentPhase() == PhaseType.DSA) {
            dsaProblemService.generateProblem(saved);
        }

        String firstQuestion = phaseQuestionService.generateFirstQuestion(saved);
        chatService.saveAiMessage(saved, firstQuestion);

        return mapToResponse(saved);
    }

    public InterviewResponse getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));
    }

    public List<InterviewResponse> getUserSessions(Long userId) {
        return sessionRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<InterviewResponse> getUserSessionsPaginated(Long userId, Pageable pageable) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public PhaseType advancePhase(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Interview session is already completed");
        }

        phaseTimerService.advancePhaseOrComplete(session, true);
        
        // Ensure DSA problem is generated if phase advances to DSA
        if (session.getCurrentPhase() == PhaseType.DSA && (session.getDsaProblemGenerated() == null || !session.getDsaProblemGenerated())) {
            dsaProblemService.generateProblem(session);
        }
        
        return session.getCurrentPhase();
    }

    @Transactional
    public InterviewResponse endSession(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            return mapToResponse(session);
        }

        PhaseResult result = new PhaseResult();
        result.setSession(session);
        result.setPhaseType(session.getCurrentPhase());
        result.setCompletedAt(LocalDateTime.now());
        phaseResultRepository.save(result);

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());

        try {
            List<PhaseType> phases = new java.util.ArrayList<>();
            String selectedPhasesStr = session.getSelectedPhases();
            if (selectedPhasesStr != null && !selectedPhasesStr.isBlank()) {
                for (String p : selectedPhasesStr.split(",")) {
                    try {
                        phases.add(PhaseType.valueOf(p.trim()));
                    } catch (IllegalArgumentException e) {
                        // ignore invalid
                    }
                }
            } else {
                phases = List.of(PhaseType.RESUME_SCREEN, PhaseType.DSA, PhaseType.HR);
            }
            
            int totalScore = 0;
            int phaseCount = 0;
            java.util.Map<String, Object> reportData = new java.util.HashMap<>();

            for (PhaseType phase : phases) {
                java.util.Map<String, Object> phaseEvaluation = scoringService.evaluatePhase(session, phase);
                if (phaseEvaluation != null && phaseEvaluation.containsKey("score")) {
                    int score = ((Number) phaseEvaluation.get("score")).intValue();
                    totalScore += score;
                    phaseCount++;
                    reportData.put(phase.name(), phaseEvaluation);
                }
            }

            if (phaseCount > 0) {
                session.setTotalScore(totalScore / phaseCount);
            }
            // IMPORTANT: only write to reportJson
            // DO NOT touch dsaProblemJson here
            // dsaProblemJson must remain intact for:
            //   - report page transcript context
            //   - debugging session history
            session.setReportJson(objectMapper.writeValueAsString(reportData));
        } catch (Exception e) {
            log.error("Failed to generate session report", e);
        }

        // dsaProblemJson is intentionally NOT modified here
        InterviewSession saved = sessionRepository.save(session);
        log.info("Session {} completed. Report saved. DSA problem preserved in dsaProblemJson.", sessionId);

        return mapToResponse(saved);
    }

    public byte[] generatePdfReport(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));

        try (org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument()) {
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            document.addPage(page);

            try (org.apache.pdfbox.pdmodel.PDPageContentStream contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(
                    document, page)) {
                contentStream.beginText();

                // PDFBox 3.x way to set Standard 14 fonts
                contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD), 20.0f);
                contentStream.setLeading(25f);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("MockMate Interview Report");
                contentStream.newLine();

                contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 12.0f);
                contentStream.showText("Session ID: " + session.getId());
                contentStream.newLine();
                contentStream.showText("Company: " + (session.getCompany() != null ? session.getCompany() : "N/A"));
                contentStream.newLine();
                contentStream.showText("Role: " + (session.getJobRole() != null ? session.getJobRole() : "N/A"));
                contentStream.newLine();
                contentStream.showText("Overall Score: "
                        + (session.getTotalScore() != null ? session.getTotalScore() : "Pending") + "/100");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD), 14.0f);
                contentStream.showText("Interview Transcript Summary");
                contentStream.newLine();
                contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 10.0f);

                List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
                int lineCount = 0;
                for (ChatMessage msg : messages) {
                    if (lineCount > 20)
                        break;
                    String text = msg.getRole() + ": " + msg.getContent();
                    if (text.length() > 80)
                        text = text.substring(0, 77) + "...";
                    try {
                        contentStream.showText(text);
                    } catch (Exception e) {
                        contentStream.showText(" [Transcript line suppressed due to encoding] ");
                    }
                    contentStream.newLine();
                    lineCount++;
                }

                contentStream.endText();
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private InterviewResponse mapToResponse(InterviewSession session) {
        java.util.List<String> phasesList = new java.util.ArrayList<>();
        if (session.getSelectedPhases() != null && !session.getSelectedPhases().isBlank()) {
            phasesList = java.util.Arrays.asList(session.getSelectedPhases().split(","));
        } else {
            phasesList = java.util.List.of("RESUME_SCREEN", "DSA", "HR");
        }

        return InterviewResponse.builder()
                .id(session.getId())
                .company(session.getCompany())
                .jobRole(session.getJobRole())
                .difficulty(session.getDifficulty() != null ? session.getDifficulty().name() : null)
                .interviewType(session.getInterviewType())
                .status(session.getStatus())
                .currentPhase(session.getCurrentPhase())
                .phaseEndTime(session.getPhaseEndTime())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .totalScore(session.getTotalScore())
                .dsaProblemJson(session.getDsaProblemJson())
                .resumeDurationMins(session.getResumeDurationMins())
                .dsaDurationMins(session.getDsaDurationMins())
                .hrDurationMins(session.getHrDurationMins())
                .selectedPhases(phasesList)
                .messages(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
                        .map(msg -> ChatMessageResponse.builder()
                                .id(msg.getId())
                                .role(msg.getRole())
                                .content(msg.getContent())
                                .timestamp(msg.getCreatedAt())
                                .phase(msg.getPhaseType())
                                .build())
                        .toList())
                .build();
    }
}
