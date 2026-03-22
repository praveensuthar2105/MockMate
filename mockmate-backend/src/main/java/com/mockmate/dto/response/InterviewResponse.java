package com.mockmate.dto.response;

import com.mockmate.model.InterviewType;
import com.mockmate.model.PhaseType;
import com.mockmate.model.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {

    private Long id;
    private String company;
    private String jobRole;
    private String difficulty;
    private InterviewType interviewType;
    private SessionStatus status;
    private PhaseType currentPhase;
    private LocalDateTime phaseEndTime;
    private LocalDateTime startedAt;
    private Integer totalScore;
    private Integer resumeDurationMins;
    private Integer dsaDurationMins;
    private Integer systemDesignDurationMins;
    private Integer hrDurationMins;
    private LocalDateTime endedAt;

    private List<ChatMessageResponse> messages;
}
