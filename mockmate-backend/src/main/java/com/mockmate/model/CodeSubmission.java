package com.mockmate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    private String language;

    @Column(columnDefinition = "TEXT")
    private String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "test_results_json", columnDefinition = "jsonb")
    private String testResultsJson;

    @Column(name = "time_complexity")
    private String timeComplexity;

    @Column(name = "space_complexity")
    private String spaceComplexity;

    @Column(name = "hints_used")
    private Integer hintsUsed = 0;

    private Integer score;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;
}
