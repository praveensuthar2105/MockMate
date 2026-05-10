package com.mockmate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSession {

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type")
    @Builder.Default
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
    @Builder.Default
    private Integer resumeDurationMins = 5;

    @Column(name = "dsa_duration_mins", columnDefinition = "integer default 30")
    @Builder.Default
    private Integer dsaDurationMins = 30;

    @Column(name = "hr_duration_mins", columnDefinition = "integer default 10")
    @Builder.Default
    private Integer hrDurationMins = 10;

    // NEW: dedicated DSA problem storage
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dsa_problem_json", columnDefinition = "jsonb")
    private String dsaProblemJson;

    // NEW: generation lock — prevents double generation
    @Column(name = "dsa_problem_generated", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean dsaProblemGenerated = false;

    // NEW: timestamp for debugging
    @Column(name = "dsa_problem_generated_at")
    private LocalDateTime dsaProblemGeneratedAt;

    @Column(name = "selected_phases")
    private String selectedPhases;
}
