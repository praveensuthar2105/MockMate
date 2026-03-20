package com.mockmate.dto.response;

import com.mockmate.model.Difficulty;
import com.mockmate.model.PhaseType;
import com.mockmate.model.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {

    private Long id;
    private String company;
    private String difficulty;
    private SessionStatus status;
    private PhaseType currentPhase;
    private LocalDateTime phaseEndTime;
    private LocalDateTime startedAt;
    private Integer totalScore;
}
