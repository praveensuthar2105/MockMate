package com.mockmate.service;

import com.mockmate.dto.response.AnalyticsOverviewResponse;
import com.mockmate.model.InterviewSession;
import com.mockmate.model.PhaseResult;
import com.mockmate.model.PhaseType;
import com.mockmate.repository.InterviewSessionRepository;
import com.mockmate.repository.PhaseResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final PhaseResultRepository phaseResultRepository;

    public AnalyticsOverviewResponse getOverview(Long userId) {
        List<InterviewSession> sessions = interviewSessionRepository.findByUserIdOrderByStartedAtDesc(userId);

        List<Integer> validScores = sessions.stream()
                .map(InterviewSession::getTotalScore)
                .filter(score -> score != null && score >= 0)
                .toList();

        int totalInterviews = sessions.size();
        int averageScore = validScores.isEmpty() ? 0 : (int) Math.round(validScores.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0));
        int highestScore = validScores.stream().mapToInt(Integer::intValue).max().orElse(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        List<AnalyticsOverviewResponse.TrendPoint> recentTrends = sessions.stream()
                .filter(session -> session.getStartedAt() != null)
                .sorted(Comparator.comparing(InterviewSession::getStartedAt))
                .skip(Math.max(0, sessions.size() - 7L))
                .map(session -> AnalyticsOverviewResponse.TrendPoint.builder()
                        .date(session.getStartedAt().format(formatter))
                        .score(session.getTotalScore() == null ? 0 : session.getTotalScore())
                        .build())
                .toList();

        List<Long> sessionIds = sessions.stream().map(InterviewSession::getId).toList();
        List<PhaseResult> phaseResults = sessionIds.isEmpty()
                ? List.of()
                : phaseResultRepository.findBySessionIdIn(sessionIds);

        Map<PhaseType, Integer> phaseAverages = phaseResults.stream()
                .filter(result -> result.getScore() != null)
                .collect(Collectors.groupingBy(
                        PhaseResult::getPhaseType,
                        Collectors.collectingAndThen(
                                Collectors.averagingInt(PhaseResult::getScore),
                                avg -> (int) Math.round(avg)
                        )
                ));

        List<AnalyticsOverviewResponse.SkillPoint> skillRadar = List.of(
                buildSkill("Algorithms", phaseAverages.getOrDefault(PhaseType.DSA, 0)),
                buildSkill("System Design", phaseAverages.getOrDefault(PhaseType.SYSTEM_DESIGN, 0)),
                buildSkill("Communication", phaseAverages.getOrDefault(PhaseType.HR, 0)),
                buildSkill("Problem Solving", phaseAverages.getOrDefault(PhaseType.RESUME_SCREEN, 0))
        );

        return AnalyticsOverviewResponse.builder()
                .totalInterviews(totalInterviews)
                .averageScore(averageScore)
                .highestScore(highestScore)
                .recentTrends(recentTrends)
                .skillRadar(skillRadar)
                .build();
    }

    private AnalyticsOverviewResponse.SkillPoint buildSkill(String subject, int score) {
        return AnalyticsOverviewResponse.SkillPoint.builder()
                .subject(subject)
                .A(score)
                .fullMark(100)
                .build();
    }
}
