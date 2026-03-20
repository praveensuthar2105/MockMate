package com.mockmate.service;

import com.mockmate.dto.request.InterviewRequest;
import com.mockmate.dto.response.InterviewResponse;
import com.mockmate.model.*;
import com.mockmate.repository.InterviewSessionRepository;
import com.mockmate.repository.PhaseResultRepository;
import com.mockmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final PhaseResultRepository phaseResultRepository;
    private final UserRepository userRepository;
    private final PhaseTimerService phaseTimerService;
    private final PhaseQuestionService phaseQuestionService;
    private final ChatService chatService;

    @Transactional
    public InterviewResponse startSession(Long userId, InterviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setCompany(request.getCompany());
        session.setDifficulty(Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setCurrentPhase(PhaseType.RESUME_SCREEN);
        session.setStartedAt(LocalDateTime.now());
        session.setPhaseEndTime(LocalDateTime.now().plusMinutes(session.getResumeDurationMins()));

        InterviewSession saved = sessionRepository.save(session);
        log.info("Interview session started: id={}, user={}, company={}", saved.getId(), userId, request.getCompany());

        // Generate opening question for the first phase (RESUME_SCREEN)
        String firstQuestion = phaseQuestionService.generateFirstQuestion(saved);
        chatService.saveAiMessage(saved, firstQuestion);

        return mapToResponse(saved);
    }

    public InterviewResponse getSession(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));
        return mapToResponse(session);
    }

    public List<InterviewResponse> getUserSessions(Long userId) {
        return sessionRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PhaseType advancePhase(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Interview session is already completed");
        }

        phaseTimerService.advancePhaseOrComplete(session);
        return session.getCurrentPhase();
    }

    @Transactional
    public InterviewResponse endSession(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));

        // Save final phase result
        PhaseResult result = new PhaseResult();
        result.setSession(session);
        result.setPhaseType(session.getCurrentPhase());
        result.setCompletedAt(LocalDateTime.now());
        phaseResultRepository.save(result);

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());

        InterviewSession saved = sessionRepository.save(session);
        log.info("Interview session ended: id={}", sessionId);

        return mapToResponse(saved);
    }

    private int getDurationForPhase(InterviewSession session, PhaseType phase) {
        return switch (phase) {
            case RESUME_SCREEN -> session.getResumeDurationMins();
            case DSA -> session.getDsaDurationMins();
            case SYSTEM_DESIGN -> session.getSystemDesignDurationMins();
            case HR -> session.getHrDurationMins();
        };
    }

    private InterviewResponse mapToResponse(InterviewSession session) {
        return InterviewResponse.builder()
                .id(session.getId())
                .company(session.getCompany())
                .difficulty(session.getDifficulty() != null ? session.getDifficulty().name() : null)
                .status(session.getStatus())
                .currentPhase(session.getCurrentPhase())
                .phaseEndTime(session.getPhaseEndTime())
                .startedAt(session.getStartedAt())
                .totalScore(session.getTotalScore())
                .build();
    }
}
