package com.mockmate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "phase_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhaseResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase_type")
    private PhaseType phaseType;

    private Integer score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feedback_json", columnDefinition = "jsonb")
    private String feedbackJson;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
