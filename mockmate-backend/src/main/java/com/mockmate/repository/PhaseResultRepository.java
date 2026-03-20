package com.mockmate.repository;

import com.mockmate.model.PhaseResult;
import com.mockmate.model.PhaseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhaseResultRepository extends JpaRepository<PhaseResult, Long> {
    List<PhaseResult> findBySessionId(Long sessionId);

    Optional<PhaseResult> findBySessionIdAndPhaseType(Long sessionId, PhaseType phaseType);
}
