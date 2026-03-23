package com.mockmate.service;

import com.mockmate.dto.request.InterviewRequest;
import com.mockmate.dto.response.InterviewResponse;
import com.mockmate.model.*;
import com.mockmate.repository.InterviewSessionRepository;
import com.mockmate.repository.PhaseResultRepository;
import com.mockmate.repository.UserRepository;
import com.mockmate.repository.ChatMessageRepository;
import com.mockmate.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public InterviewResponse createSession(Long userId, InterviewRequest request) {
        Objects.requireNonNull(userId, "userId must not be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getId() == null) {
            throw new RuntimeException("User ID is null");
        }

        InterviewSession session = new InterviewSession();
        session.setUser(user);
        session.setCompany(request.getCompany());
        session.setJobRole(request.getJobRole());
        session.setDifficulty(Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        session.setInterviewType(request.getType() != null ? request.getType() : InterviewType.FULL_MOCK);
        session.setStatus(SessionStatus.IN_PROGRESS);

        PhaseType initialPhase = switch (session.getInterviewType()) {
            case DSA_ONLY -> PhaseType.DSA;
            case SYSTEM_DESIGN_ONLY -> PhaseType.SYSTEM_DESIGN;
            case HR_ONLY -> PhaseType.HR;
            default -> PhaseType.RESUME_SCREEN;
        };
        session.setCurrentPhase(initialPhase);

        if (request.getResumeDurationMins() != null) {
            if (request.getResumeDurationMins() < 3 || request.getResumeDurationMins() > 15) {
                throw new IllegalArgumentException("Resume duration must be between 3 and 15 minutes");
            }
            session.setResumeDurationMins(request.getResumeDurationMins());
        } else {
            session.setResumeDurationMins(5);
        }

        if (request.getDsaDurationMins() != null) {
            if (request.getDsaDurationMins() < 15 || request.getDsaDurationMins() > 60) {
                throw new IllegalArgumentException("DSA duration must be between 15 and 60 minutes");
            }
            session.setDsaDurationMins(request.getDsaDurationMins());
        } else {
            session.setDsaDurationMins(30);
        }

        if (request.getSystemDesignDurationMins() != null) {
            if (request.getSystemDesignDurationMins() < 10 || request.getSystemDesignDurationMins() > 30) {
                throw new IllegalArgumentException("System Design duration must be between 10 and 30 minutes");
            }
            session.setSystemDesignDurationMins(request.getSystemDesignDurationMins());
        } else {
            session.setSystemDesignDurationMins(15);
        }

        if (request.getHrDurationMins() != null) {
            if (request.getHrDurationMins() < 5 || request.getHrDurationMins() > 20) {
                throw new IllegalArgumentException("HR duration must be between 5 and 20 minutes");
            }
            session.setHrDurationMins(request.getHrDurationMins());
        } else {
            session.setHrDurationMins(10);
        }

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
            case SYSTEM_DESIGN -> session.getSystemDesignDurationMins();
            case HR -> session.getHrDurationMins();
        };
        session.setPhaseEndTime(LocalDateTime.now().plusMinutes(duration));

        InterviewSession saved = sessionRepository.save(session);
        log.info("Interview session started: id={}", saved.getId());

        String firstQuestion = phaseQuestionService.generateFirstQuestion(saved);
        chatService.saveAiMessage(saved, firstQuestion);

        return mapToResponse(saved);
    }

    public InterviewResponse getSession(Long sessionId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));
        return mapToResponse(session);
    }

    public List<InterviewResponse> getUserSessions(Long userId) {
        Objects.requireNonNull(userId, "userId must not be null");
        return sessionRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<InterviewResponse> getUserSessionsPaginated(Long userId, Pageable pageable) {
        Objects.requireNonNull(userId, "userId must not be null");
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public PhaseType advancePhase(Long sessionId) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
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
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Interview session not found"));

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

    private InterviewResponse mapToResponse(InterviewSession session) {
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
                .resumeDurationMins(session.getResumeDurationMins())
                .dsaDurationMins(session.getDsaDurationMins())
                .systemDesignDurationMins(session.getSystemDesignDurationMins())
                .hrDurationMins(session.getHrDurationMins())
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
