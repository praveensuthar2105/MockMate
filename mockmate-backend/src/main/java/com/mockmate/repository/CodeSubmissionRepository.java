package com.mockmate.repository;

import com.mockmate.model.CodeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {
    List<CodeSubmission> findBySessionId(Long sessionId);

    List<CodeSubmission> findBySessionIdOrderBySubmittedAtDesc(Long sessionId);
}
