package com.mockmate.service;

import com.mockmate.dto.response.AnalyticsResponse;
import com.mockmate.dto.response.ScoreTrend;
import com.mockmate.dto.response.SkillDistribution;
import com.mockmate.model.InterviewSession;
import com.mockmate.repository.InterviewSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

        private final InterviewSessionRepository sessionRepository;

        public AnalyticsResponse getUserAnalytics(Long userId) {
                List<InterviewSession> sessions = sessionRepository.findByUserId(userId);

                int totalInterviews = sessions.size();
                int avgScore = sessions.stream()
                                .filter(s -> s.getTotalScore() != null)
                                .mapToInt(InterviewSession::getTotalScore)
                                .average()
                                .isPresent() ? (int) sessions.stream()
                                                .filter(s -> s.getTotalScore() != null)
                                                .mapToInt(InterviewSession::getTotalScore)
                                                .average()
                                                .getAsDouble() : 0;

                List<ScoreTrend> trends = new ArrayList<>();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

                sessions.stream()
                                .filter(s -> s.getStartedAt() != null && s.getTotalScore() != null)
                                .sorted((s1, s2) -> s1.getStartedAt().compareTo(s2.getStartedAt()))
                                .limit(10)
                                .forEach(s -> trends.add(
                                                new ScoreTrend(s.getStartedAt().format(formatter), s.getTotalScore())));

                // Mock skill distribution for now
                List<SkillDistribution> skills = List.of(
                                new SkillDistribution("Communication", 75, 100),
                                new SkillDistribution("Technical", 80, 100),
                                new SkillDistribution("Problem Solving", avgScore > 0 ? avgScore : 70, 100),
                                new SkillDistribution("Leadership", 65, 100),
                                new SkillDistribution("HR Alignment", 85, 100));

                return AnalyticsResponse.builder()
                                .totalInterviews(totalInterviews)
                                .averageScore(avgScore)
                                .topSkill("Technical")
                                .scoreTrends(trends)
                                .skillDistributions(skills)
                                .build();
        }
}
