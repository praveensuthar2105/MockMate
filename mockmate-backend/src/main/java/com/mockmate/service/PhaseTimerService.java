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

    public PhaseTimerService(InterviewSessionRepository sessionRepository,
            SimpMessagingTemplate simpMessagingTemplate,
            PhaseQuestionService phaseQuestionService,
            ChatService chatService,
            com.mockmate.repository.PhaseResultRepository phaseResultRepository,
            DsaProblemService dsaProblemService,
            @Lazy InterviewService interviewService) {
        this.sessionRepository = sessionRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.phaseQuestionService = phaseQuestionService;
        this.chatService = chatService;
        this.phaseResultRepository = phaseResultRepository;
        this.dsaProblemService = dsaProblemService;
        this.interviewService = interviewService;
    }

    private final java.util.concurrent.ConcurrentHashMap<Long, Object> transitionLocks = new java.util.concurrent.ConcurrentHashMap<>();

    @Scheduled(fixedRate = 1000)
    public void checkTimers() {
        List<InterviewSession> activeSessions = sessionRepository.findAll();

        for (InterviewSession session : activeSessions) {
            if (session.getStatus() != SessionStatus.IN_PROGRESS || session.getPhaseEndTime() == null) {
                continue;
            }

            long secondsRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getPhaseEndTime());

            if (secondsRemaining <= 0) {
                advancePhaseOrComplete(session, false);
            } else if (secondsRemaining % 30 == 0 || secondsRemaining <= 10) {
                simpMessagingTemplate.convertAndSend(
                        "/topic/session/" + session.getId(),
                        WsEvent.timerUpdate((int) secondsRemaining));
            }
        }
    }

    public void advancePhaseOrComplete(InterviewSession inputSession, boolean isManualTrigger) {
        Long sessionId = inputSession.getId();
        Object lock = transitionLocks.computeIfAbsent(sessionId, k -> new Object());

        synchronized (lock) {
            // Re-fetch to guarantee we have the absolutely latest database state
            InterviewSession session = sessionRepository.findById(sessionId).orElseThrow();

            // If triggered by timer, double-check that another thread didn't already advance it
            if (!isManualTrigger && session.getPhaseEndTime() != null) {
                long secondsRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getPhaseEndTime());
                if (secondsRemaining > 0) {
                    log.info("Session {} already advanced by another thread. Skipping.", sessionId);
                    return;
                }
            }

            PhaseType next = getNextPhase(session);

            if (next == null) {
                com.mockmate.model.PhaseResult result = new com.mockmate.model.PhaseResult();
                result.setSession(session);
                result.setPhaseType(session.getCurrentPhase());
                result.setCompletedAt(LocalDateTime.now());
                phaseResultRepository.save(result);

                interviewService.endSession(session.getId());

                simpMessagingTemplate.convertAndSend(
                        "/topic/session/" + session.getId(),
                        WsEvent.phaseChange(null));
            } else {
                com.mockmate.model.PhaseResult result = new com.mockmate.model.PhaseResult();
                result.setSession(session);
                result.setPhaseType(session.getCurrentPhase());
                result.setCompletedAt(LocalDateTime.now());
                phaseResultRepository.save(result);

                session.setCurrentPhase(next);
                int duration = getDurationForPhase(session, next);
                session.setPhaseEndTime(LocalDateTime.now().plusMinutes(duration));
                session = sessionRepository.saveAndFlush(session);

                if (next == PhaseType.DSA) {
                    dsaProblemService.generateProblem(session);
                }

                String firstQuestion = phaseQuestionService.generateFirstQuestion(session);
                chatService.saveAiMessage(session, firstQuestion);

                simpMessagingTemplate.convertAndSend(
                        "/topic/session/" + session.getId(),
                        WsEvent.phaseChange(next));

                simpMessagingTemplate.convertAndSend(
                        "/topic/session/" + session.getId(),
                        WsEvent.message(firstQuestion));
            }
        }
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
            case SYSTEM_DESIGN -> 15; // Fallback
            case HR -> session.getHrDurationMins();
        };
    }
}
