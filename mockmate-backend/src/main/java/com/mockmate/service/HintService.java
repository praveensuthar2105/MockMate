package com.mockmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockmate.dto.code.DsaProblem;
import com.mockmate.dto.code.ProblemHint;
import com.mockmate.model.CodeSubmission;
import com.mockmate.model.InterviewSession;
import com.mockmate.repository.CodeSubmissionRepository;
import com.mockmate.repository.InterviewSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HintService {

    private final InterviewSessionRepository sessionRepository;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public String getHint(Long sessionId, int level) {
        if (level < 1 || level > 3) {
            throw new IllegalArgumentException("Hint level must be between 1 and 3");
        }

        InterviewSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!"IN_PROGRESS".equals(session.getStatus().name()) || !"DSA".equals(session.getCurrentPhase().name())) {
            throw new IllegalStateException("Hints are only available during the active DSA phase");
        }

        String reportJson = session.getReportJson();
        if (reportJson == null || reportJson.isEmpty()) {
            throw new IllegalStateException("No problem found for this session");
        }

        DsaProblem problem;
        try {
            problem = objectMapper.readValue(reportJson, DsaProblem.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DSA problem", e);
        }

        String hintText = "Hint not found";
        boolean foundHint = false;
        if (problem.getHints() != null) {
            for (ProblemHint hint : problem.getHints()) {
                if (hint.getLevel() == level) {
                    hintText = hint.getHint();
                    foundHint = true;
                    break;
                }
            }
        }

        List<CodeSubmission> submissions = codeSubmissionRepository.findBySessionIdOrderBySubmittedAtDesc(sessionId);
        CodeSubmission activeSubmission;

        if (!submissions.isEmpty()) {
            activeSubmission = submissions.get(0);
            if (activeSubmission.getSubmitted() != null && activeSubmission.getSubmitted()) {
                throw new IllegalStateException("Code already submitted for this session");
            }
        } else {
            activeSubmission = new CodeSubmission();
            activeSubmission.setSession(session);
            activeSubmission.setHintsUsed(0);
        }

        if (foundHint) {
            int mask = activeSubmission.getHintsUsed() != null ? activeSubmission.getHintsUsed() : 0;
            int bit = 1 << (level - 1);

            if ((mask & bit) == 0) {
                mask |= bit;
                activeSubmission.setHintsUsed(mask);
                codeSubmissionRepository.save(activeSubmission);
                log.info("Session {} unlocked hint level {}. Score deduction mapped.", sessionId, level);
            } else {
                log.info("Session {} requested hint level {} again. No deduction.", sessionId, level);
            }
        } else {
            log.info("Session {} could not find hint level {}. No deduction.", sessionId, level);
        }

        return hintText;
    }
}
