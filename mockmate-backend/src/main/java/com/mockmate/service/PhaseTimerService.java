package com.mockmate.service;

import com.mockmate.dto.ws.WsEvent;
import com.mockmate.model.InterviewSession;
import com.mockmate.model.PhaseType;
import com.mockmate.model.SessionStatus;
import com.mockmate.repository.InterviewSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhaseTimerService {

    private final InterviewSessionRepository sessionRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final PhaseQuestionService phaseQuestionService;
    private final ChatService chatService;
    private final com.mockmate.repository.PhaseResultRepository phaseResultRepository;

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void checkTimers() {
        List<InterviewSession> activeSessions = sessionRepository.findAll(); // Optimization: Create a query for
                                                                             // IN_PROGRESS and phaseEndTime IS NOT NULL

        for (InterviewSession session : activeSessions) {
            if (session.getStatus() != SessionStatus.IN_PROGRESS || session.getPhaseEndTime() == null) {
                continue;
            }

            long secondsRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getPhaseEndTime());

            if (secondsRemaining <= 0) {
                // Timer expired — auto advance phase
                advancePhaseOrComplete(session);
            } else if (secondsRemaining % 30 == 0 || secondsRemaining <= 10) {
                // Broadcast timer update (every 30s + every second in last 10s)
                simpMessagingTemplate.convertAndSend(
                        "/topic/session/" + session.getId(),
                        WsEvent.timerUpdate((int) secondsRemaining));
            }
        }
    }

    @Transactional
    public void advancePhaseOrComplete(InterviewSession session) {
        PhaseType next = getNextPhase(session.getCurrentPhase());

        if (next == null) {
            // Save result for current phase
            com.mockmate.model.PhaseResult result = new com.mockmate.model.PhaseResult();
            result.setSession(session);
            result.setPhaseType(session.getCurrentPhase());
            result.setCompletedAt(LocalDateTime.now());
            phaseResultRepository.save(result);

            // All phases done — complete the session
            session.setStatus(SessionStatus.COMPLETED);
            session.setEndedAt(LocalDateTime.now());
            sessionRepository.save(session);

            simpMessagingTemplate.convertAndSend(
                    "/topic/session/" + session.getId(),
                    WsEvent.phaseChange(null)); // null = session complete
        } else {
            // Save result for current phase
            com.mockmate.model.PhaseResult result = new com.mockmate.model.PhaseResult();
            result.setSession(session);
            result.setPhaseType(session.getCurrentPhase());
            result.setCompletedAt(LocalDateTime.now());
            phaseResultRepository.save(result);

            // Advance to next phase
            session.setCurrentPhase(next);
            int duration = getDurationForPhase(session, next);
            session.setPhaseEndTime(LocalDateTime.now().plusMinutes(duration));
            sessionRepository.save(session);

            // Generate opening question for new phase
            String firstQuestion = phaseQuestionService.generateFirstQuestion(session);

            // Save and broadcast
            chatService.saveAiMessage(session, firstQuestion);

            simpMessagingTemplate.convertAndSend(
                    "/topic/session/" + session.getId(),
                    WsEvent.phaseChange(next));

            simpMessagingTemplate.convertAndSend(
                    "/topic/session/" + session.getId(),
                    WsEvent.message(firstQuestion));
        }
    }

    private PhaseType getNextPhase(PhaseType current) {
        return switch (current) {
            case RESUME_SCREEN -> PhaseType.DSA;
            case DSA -> PhaseType.SYSTEM_DESIGN;
            case SYSTEM_DESIGN -> PhaseType.HR;
            case HR -> null; // end of interview
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
