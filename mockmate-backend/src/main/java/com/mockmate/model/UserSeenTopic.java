package com.mockmate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_seen_topics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSeenTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "topic_title", nullable = false)
    private String topicTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "phase_type", nullable = false)
    private PhaseType phaseType;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false)
    private String company;

    @Column
    private String category;

    @Column
    private Integer score;

    @CreationTimestamp
    @Column(name = "seen_at")
    private LocalDateTime seenAt;
}
