package com.mockmate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSession {

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type")
    private InterviewType interviewType = InterviewType.FULL_MOCK;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String company;

    @Column(name = "job_role")
    private String jobRole;

    @Convert(converter = com.mockmate.model.converter.DifficultyConverter.class)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase")
    private PhaseType currentPhase;

    @Column(name = "phase_end_time")
    private LocalDateTime phaseEndTime;

    @Column(name = "total_score")
    private Integer totalScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_json", columnDefinition = "jsonb")
    private String reportJson;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "resume_duration_mins", columnDefinition = "integer default 5")
    private Integer resumeDurationMins = 5;

    @Column(name = "dsa_duration_mins", columnDefinition = "integer default 30")
    private Integer dsaDurationMins = 30;

    @Column(name = "system_design_duration_mins", columnDefinition = "integer default 15")
    private Integer systemDesignDurationMins = 15;

    @Column(name = "hr_duration_mins", columnDefinition = "integer default 10")
    private Integer hrDurationMins = 10;
}
