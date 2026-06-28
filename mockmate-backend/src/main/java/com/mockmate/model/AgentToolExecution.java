package com.mockmate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_tool_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentToolExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(name = "agent_name", nullable = false, length = 100)
    private String agentName;

    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    @Column(name = "input_arguments", columnDefinition = "TEXT")
    private String inputArguments;

    @Column(name = "output_response", columnDefinition = "TEXT")
    private String outputResponse;

    @CreationTimestamp
    @Column(name = "executed_at", updatable = false)
    private LocalDateTime executedAt;
}
