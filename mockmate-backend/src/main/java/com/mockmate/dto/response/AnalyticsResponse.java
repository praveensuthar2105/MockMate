package com.mockmate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private int totalInterviews;
    private int averageScore;
    private String topSkill;
    private List<ScoreTrend> scoreTrends;
    private List<SkillDistribution> skillDistributions;
}
