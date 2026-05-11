package com.mockmate.repository;

import com.mockmate.model.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    @Query("SELECT s FROM InterviewSession s JOIN FETCH s.user WHERE s.user.id = :userId")
    List<InterviewSession> findByUserId(@Param("userId") Long userId);

    List<InterviewSession> findByUserIdAndStatus(Long userId, com.mockmate.model.SessionStatus status);

    @Query("SELECT s FROM InterviewSession s JOIN FETCH s.user WHERE s.user.id = :userId ORDER BY s.startedAt DESC")
    List<InterviewSession> findByUserIdOrderByStartedAtDesc(@Param("userId") Long userId);

    @Query(value = "SELECT s FROM InterviewSession s JOIN FETCH s.user WHERE s.user.id = :userId ORDER BY s.startedAt DESC",
           countQuery = "SELECT count(s) FROM InterviewSession s WHERE s.user.id = :userId")
    Page<InterviewSession> findByUserIdOrderByStartedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InterviewSession s WHERE s.id = :id")
    Optional<InterviewSession> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT s FROM InterviewSession s JOIN FETCH s.user WHERE s.id = :id")
    Optional<InterviewSession> findByIdWithUser(@Param("id") Long id);
    List<InterviewSession> findByStatusAndPhaseEndTimeIsNotNull(com.mockmate.model.SessionStatus status);
}