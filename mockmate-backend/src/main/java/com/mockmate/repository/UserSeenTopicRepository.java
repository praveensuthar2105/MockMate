package com.mockmate.repository;

import com.mockmate.model.PhaseType;
import com.mockmate.model.UserSeenTopic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSeenTopicRepository extends JpaRepository<UserSeenTopic, Long> {

    List<UserSeenTopic> findByUserIdAndPhaseTypeOrderBySeenAtDesc(Long userId, PhaseType phaseType, Pageable pageable);

    @Query("SELECT t.topicTitle FROM UserSeenTopic t WHERE t.user.id = :userId AND t.phaseType = :phaseType ORDER BY t.seenAt DESC")
    List<String> findRecentTopicTitles(Long userId, PhaseType phaseType, Pageable pageable);

    boolean existsByUserIdAndTopicTitleIgnoreCaseAndPhaseType(Long userId, String topicTitle, PhaseType phaseType);
}
