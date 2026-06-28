package com.mockmate.service;

import com.mockmate.dto.ws.WsEvent;
import com.mockmate.model.InterviewSession;
import com.mockmate.model.PhaseType;
import com.mockmate.model.SessionStatus;
import com.mockmate.repository.InterviewSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class PhaseTimerService {

    private final InterviewSessionRepository sessionRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final PhaseQuestionService phaseQuestionService;
    private final ChatService chatService;
    private final com.mockmate.repository.PhaseResultRepository phaseResultRepository;
    private final DsaProblemService dsaProblemService;
    private final InterviewService interviewService;
    private final PhaseTimerService self;

    public PhaseTimerService(InterviewSessionRepository sessionRepository,
            SimpMessagingTemplate simpMessagingTemplate,
            PhaseQuestionService phaseQuestionService,
            ChatService chatService,
            com.mockmate.repository.PhaseResultRepository phaseResultRepository,
            DsaProblemService dsaProblemService,
            @Lazy InterviewService interviewService,
            @Lazy PhaseTimerService self) {
        this.sessionRepository = sessionRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.phaseQuestionService = phaseQuestionService;
        this.chatService = chatService;
        this.phaseResultRepository = phaseResultRepository;
        this.dsaProblemService = dsaProblemService;
        this.interviewService = interviewService;
        this.self = self;
    }

    @Scheduled(fixedRate = 1000)
    public void checkTimers() {
        List<InterviewSession> activeSessions = sessionRepository.findByStatusAndPhaseEndTimeIsNotNull(SessionStatus.IN_PROGRESS);

        for (InterviewSession session : activeSessions) {
            if (session.getStatus() != SessionStatus.IN_PROGRESS || session.getPhaseEndTime() == null) {
                continue;
            }

            long secondsRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getPhaseEndTime());

            if (secondsRemaining <= 0) {
                try {
                    advancePhaseOrComplete(session, false);
                } catch (Exception e) {
                    log.error("Failed to advance phase for session {}", session.getId(), e);
                }
            } else if (secondsRemaining % 30 == 0 || secondsRemaining <= 10) {
                simpMessagingTemplate.convertAndSend(
                        "/topic/session/" + session.getId(),
                        WsEvent.timerUpdate((int) secondsRemaining));
            }
        }
    }

    public void advancePhaseOrComplete(InterviewSession inputSession, boolean isManualTrigger) {
        Long sessionId = inputSession.getId();

        // 1. Perform database state transition updates under a pessimistic write lock transaction via proxy
        InterviewSession session = self.performPhaseTransitionDbUpdates(sessionId, isManualTrigger);
        if (session == null) {
            return; // State already advanced or no action needed
        }

        PhaseType next = session.getCurrentPhase();

        // 2. If transition led to completion, run scoring and report (outside transaction block)
        if (session.getStatus() == SessionStatus.COMPLETED) {
            interviewService.endSession(sessionId);
            simpMessagingTemplate.convertAndSend(
                    "/topic/session/" + sessionId,
                    WsEvent.phaseChange(null));
        } else {
            // 3. Otherwise, we transitioned to a new phase. Generate problem if DSA (outside active transaction)
            if (next == PhaseType.DSA) {
                dsaProblemService.generateProblem(session);
                // Re-fetch updated problem state
                session = sessionRepository.findById(sessionId).orElseThrow();
            }

            // 4. Generate first question (outside active transaction)
            String firstQuestion = session.getPreGeneratedOpener();
            if (firstQuestion == null || firstQuestion.isBlank()) {
                firstQuestion = phaseQuestionService.generateFirstQuestion(session);
            } else {
                log.info("Using cached pre-generated opener for session {}, phase {}", sessionId, next);
                self.clearPreGeneratedOpener(sessionId);
            }

            // 5. Persist message and broadcast (in a short transaction) via proxy
            self.saveOpeningMessageAndNotify(sessionId, firstQuestion, next);

            // 6. Trigger pre-generation for the next phase asynchronously
            self.preGenerateNextPhaseDataAsync(sessionId);
        }
    }

    @Transactional
    public InterviewSession performPhaseTransitionDbUpdates(Long sessionId, boolean isManualTrigger) {
        // Enforce postgres row-level lock
        InterviewSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            return null;
        }

        // Double-check remaining time for safety under concurrent threads
        if (!isManualTrigger && session.getPhaseEndTime() != null) {
            long secondsRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getPhaseEndTime());
            if (secondsRemaining > 0) {
                log.info("Session {} already advanced by another thread. Skipping.", sessionId);
                return null;
            }
        }

        PhaseType next = getNextPhase(session);

        com.mockmate.model.PhaseResult result = new com.mockmate.model.PhaseResult();
        result.setSession(session);
        result.setPhaseType(session.getCurrentPhase());
        result.setCompletedAt(LocalDateTime.now());
        phaseResultRepository.save(result);

        if (next == null) {
            session.setStatus(SessionStatus.COMPLETED);
            session.setEndedAt(LocalDateTime.now());
        } else {
            session.setCurrentPhase(next);
            int duration = getDurationForPhase(session, next);
            session.setPhaseEndTime(LocalDateTime.now().plusMinutes(duration));
        }

        return sessionRepository.saveAndFlush(session);
    }

    @Transactional
    public void saveOpeningMessageAndNotify(Long sessionId, String firstQuestion, PhaseType newPhase) {
        InterviewSession session = sessionRepository.findById(sessionId).orElseThrow();
        chatService.saveAiMessage(session, firstQuestion);

        simpMessagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                WsEvent.phaseChange(newPhase));

        simpMessagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                WsEvent.message(firstQuestion));
    }

    private PhaseType getNextPhase(InterviewSession session) {
        PhaseType current = session.getCurrentPhase();

        String selectedPhasesStr = session.getSelectedPhases();
        if (selectedPhasesStr != null && !selectedPhasesStr.isBlank()) {
            java.util.List<String> phases = java.util.Arrays.asList(selectedPhasesStr.split(","));
            int currentIndex = phases.indexOf(current.name());
            if (currentIndex != -1 && currentIndex + 1 < phases.size()) {
                try {
                    return PhaseType.valueOf(phases.get(currentIndex + 1).trim());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
            return null;
        }

        // Fallback if selectedPhases is somehow missing
        if (session.getInterviewType() == com.mockmate.model.InterviewType.DSA_ONLY && current == PhaseType.DSA)
            return null;
        if (session.getInterviewType() == com.mockmate.model.InterviewType.HR_ONLY && current == PhaseType.HR)
            return null;

        return switch (current) {
            case RESUME_SCREEN -> PhaseType.DSA;
            case DSA -> PhaseType.HR;
            case SYSTEM_DESIGN -> PhaseType.HR;
            case HR -> null;
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

    @Transactional
    public void clearPreGeneratedOpener(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setPreGeneratedOpener(null);
            sessionRepository.save(session);
        }
    }

    @Transactional
    public void savePreGeneratedOpener(Long sessionId, String opener) {
        InterviewSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setPreGeneratedOpener(opener);
            sessionRepository.save(session);
        }
    }

    public void preGenerateNextPhaseDataAsync(Long sessionId) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // Wait a small delay to let transaction commit
                Thread.sleep(150);

                InterviewSession session = sessionRepository.findById(sessionId).orElse(null);
                if (session == null) return;

                PhaseType next = getNextPhase(session);
                if (next == null) return;

                log.info("Starting background pre-generation of next phase {} for session {}", next, sessionId);

                // 1. Pre-generate DSA problem if next round is DSA
                if (next == PhaseType.DSA && (session.getDsaProblemGenerated() == null || !session.getDsaProblemGenerated())) {
                    dsaProblemService.generateProblem(sessionId);
                    // Refresh session reference
                    session = sessionRepository.findById(sessionId).orElseThrow();
                }

                // 2. Pre-generate opener for the next phase
                String opener = phaseQuestionService.generateFirstQuestionForPhase(session, next);

                // 3. Save pre-generated opener
                self.savePreGeneratedOpener(sessionId, opener);
                log.info("Successfully pre-generated and cached opener for next phase {} of session {}", next, sessionId);
            } catch (Exception e) {
                log.error("Failed to pre-generate next phase data for session {}", sessionId, e);
            }
        });
    }
}
