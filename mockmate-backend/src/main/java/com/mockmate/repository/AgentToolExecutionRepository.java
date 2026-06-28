package com.mockmate.repository;

import com.mockmate.model.AgentToolExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentToolExecutionRepository extends JpaRepository<AgentToolExecution, Long> {
    List<AgentToolExecution> findBySessionId(Long sessionId);
    List<AgentToolExecution> findBySessionIdOrderByExecutedAtAsc(Long sessionId);
}
