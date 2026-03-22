package com.mockmate.repository;

import com.mockmate.model.CodeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {
    List<CodeSubmission> findBySessionId(Long sessionId);

    List<CodeSubmission> findBySessionIdOrderBySubmittedAtDesc(Long sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CodeSubmission c WHERE c.session.id = :sessionId ORDER BY c.submittedAt DESC")
    List<CodeSubmission> findBySessionIdOrderBySubmittedAtDescForUpdate(@Param("sessionId") Long sessionId);
}
