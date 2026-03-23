package com.mockmate.controller;

import com.mockmate.dto.code.DsaProblem;
import com.mockmate.dto.code.ExecutionResult;
import com.mockmate.dto.code.TestCase;
import com.mockmate.dto.request.CodeRunRequest;
import com.mockmate.dto.request.CodeSubmitRequest;
import com.mockmate.dto.request.HintRequest;
import com.mockmate.dto.response.CodeEvaluation;
import com.mockmate.dto.response.DsaStatusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockmate.model.CodeSubmission;
import com.mockmate.model.InterviewSession;
import com.mockmate.repository.CodeSubmissionRepository;
import com.mockmate.repository.InterviewSessionRepository;
import com.mockmate.service.CodeEvaluationService;
import com.mockmate.service.CodeExecutionService;
import com.mockmate.service.DsaProblemService;
import com.mockmate.service.HintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/code")
@RequiredArgsConstructor
@Slf4j
public class CodeController {

    private final CodeExecutionService codeExecutionService;
    private final CodeEvaluationService codeEvaluationService;
    private final DsaProblemService dsaProblemService;
    private final HintService hintService;
    private final InterviewSessionRepository sessionRepository;
    private final CodeSubmissionRepository codeSubmissionRepository;

    @GetMapping("/problem/{sessionId}")
    public ResponseEntity<DsaStatusResponse> getProblem(@PathVariable Long sessionId, Authentication authentication) {
        InterviewSession session = validateSessionOwnershipAndPhase(sessionId, authentication.getName());
        DsaProblem problem = dsaProblemService.generateProblem(session);

        DsaStatusResponse response = new DsaStatusResponse();

        DsaProblem candidateView = new DsaProblem();
        candidateView.setTitle(problem.getTitle());
        candidateView.setDescription(problem.getDescription());
        candidateView.setConstraints(problem.getConstraints());
        candidateView.setExamples(problem.getExamples());
        candidateView.setDifficulty(problem.getDifficulty());

        // Expose only starter codes to frontend, never test runners
        candidateView.setJavaStarterCode(problem.getJavaStarterCode());
        candidateView.setPythonStarterCode(problem.getPythonStarterCode());
        candidateView.setJavascriptStarterCode(problem.getJavascriptStarterCode());

        response.setProblem(candidateView);

        List<CodeSubmission> submissions = codeSubmissionRepository.findBySessionIdOrderBySubmittedAtDesc(sessionId);
        if (!submissions.isEmpty()) {
            CodeSubmission latest = submissions.get(0);
            response.setSubmitted(latest.getSubmitted() != null && latest.getSubmitted());
            if (latest.getEvaluationJson() != null) {
                try {
                    CodeEvaluation eval = new ObjectMapper().readValue(latest.getEvaluationJson(),
                            CodeEvaluation.class);
                    response.setEvaluation(eval);
                } catch (Exception e) {
                    log.warn("Failed to parse existing evaluation JSON", e);
                }
            }
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/run")
    public ResponseEntity<ExecutionResult> runCode(@Valid @RequestBody CodeRunRequest request,
            Authentication authentication) {
        InterviewSession session = validateSessionOwnershipAndPhase(request.getSessionId(), authentication.getName());
        DsaProblem problem = dsaProblemService.generateProblem(session);

        List<TestCase> visibleTests = new java.util.ArrayList<>(problem.getExamples().stream()
                .map(ex -> {
                    TestCase tc = new TestCase();
                    tc.setInput(ex.getInput());
                    tc.setExpectedOutput(ex.getOutput());
                    tc.setDescription(ex.getExplanation());
                    return tc;
                }).toList());

        if (request.getCustomInput() != null && !request.getCustomInput().isBlank()) {
            TestCase customTc = new TestCase();
            customTc.setInput(request.getCustomInput());
            customTc.setDescription("Custom Test Case");
            visibleTests.add(0, customTc);
        }

        String testRunner = getTestRunner(request.getLanguage(), problem);
        ExecutionResult result = codeExecutionService.execute(request.getLanguage(), request.getCode(), visibleTests,
                testRunner);

        // Persist as draft submission so AI interviewer has context
        persistDraftSubmission(session, request.getLanguage(), request.getCode(), result);

        return ResponseEntity.ok(result);
    }

    private void persistDraftSubmission(InterviewSession session, String language, String code,
            ExecutionResult result) {
        try {
            List<CodeSubmission> submissions = codeSubmissionRepository
                    .findBySessionIdOrderBySubmittedAtDesc(session.getId());

            CodeSubmission submission;
            if (!submissions.isEmpty()
                    && (submissions.get(0).getSubmitted() == null || !submissions.get(0).getSubmitted())) {
                submission = submissions.get(0);
            } else {
                submission = new CodeSubmission();
                submission.setSession(session);
                submission.setHintsUsed(0);
            }

            submission.setLanguage(language);
            submission.setCode(code);
            submission.setSubmitted(false);
            submission.setSubmittedAt(java.time.LocalDateTime.now());

            ObjectMapper mapper = new ObjectMapper();
            submission.setTestResultsJson(mapper.writeValueAsString(result));

            codeSubmissionRepository.save(submission);
        } catch (Exception e) {
            log.error("Failed to persist draft submission", e);
        }
    }

    @PostMapping("/submit")
    @Transactional
    public ResponseEntity<CodeEvaluation> submitCode(@Valid @RequestBody CodeSubmitRequest request,
            Authentication authentication) {
        InterviewSession session = validateSessionOwnershipAndPhase(request.getSessionId(), authentication.getName());

        List<CodeSubmission> submissions = codeSubmissionRepository
                .findBySessionIdOrderBySubmittedAtDescForUpdate(session.getId());
        CodeSubmission activeSubmission;
        if (!submissions.isEmpty()) {
            activeSubmission = submissions.get(0);
        } else {
            activeSubmission = new CodeSubmission();
            activeSubmission.setSession(session);
            activeSubmission.setHintsUsed(0);
        }

        DsaProblem problem = dsaProblemService.generateProblem(session);
        String testRunner = getTestRunner(request.getLanguage().name(), problem);
        ExecutionResult executionResult = codeExecutionService.execute(request.getLanguage().name(), request.getCode(),
                problem.getTestCases(), testRunner);

        CodeEvaluation evaluation = codeEvaluationService.evaluate(request.getCode(), request.getLanguage().name(),
                problem,
                executionResult, activeSubmission);

        return ResponseEntity.ok(evaluation);
    }

    @PostMapping("/hint")
    public ResponseEntity<Map<String, Object>> getHint(@Valid @RequestBody HintRequest request,
            Authentication authentication) {
        validateSessionOwnershipAndPhase(request.getSessionId(), authentication.getName());
        String hint = hintService.getHint(request.getSessionId(), request.getLevel());

        int mask = getHintsMask(request.getSessionId());
        int hintsRemaining = 3 - Integer.bitCount(mask);

        return ResponseEntity.ok(Map.of(
                "hint", hint,
                "level", request.getLevel(),
                "hintsRemaining", hintsRemaining));
    }

    private int getHintsMask(Long sessionId) {
        List<CodeSubmission> submissions = codeSubmissionRepository.findBySessionIdOrderBySubmittedAtDesc(sessionId);
        if (submissions.isEmpty())
            return 0;
        return submissions.get(0).getHintsUsed() != null ? submissions.get(0).getHintsUsed() : 0;
    }

    private String getTestRunner(String language, DsaProblem problem) {
        if ("JAVA".equalsIgnoreCase(language))
            return problem.getJavaTestRunner();
        if ("PYTHON".equalsIgnoreCase(language))
            return problem.getPythonTestRunner();
        if ("JAVASCRIPT".equalsIgnoreCase(language))
            return problem.getJavascriptTestRunner();
        return null;
    }

    private InterviewSession validateSessionOwnershipAndPhase(Long sessionId, String email) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized access to session");
        }

        if (!"IN_PROGRESS".equals(session.getStatus().name()) || !"DSA".equals(session.getCurrentPhase().name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Coding actions are only available during the active DSA phase");
        }

        return session;
    }
}
