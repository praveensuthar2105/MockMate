package com.mockmate.agent.tool;

import dev.langchain4j.agent.tool.Tool;
import com.mockmate.service.CodeExecutionService;
import com.mockmate.repository.InterviewSessionRepository;
import com.mockmate.model.InterviewSession;
import com.mockmate.dto.code.DsaProblem;
import com.mockmate.dto.code.ExecutionResult;
import com.mockmate.service.context.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class SandboxExecutionTool {

    private final CodeExecutionService executionService;
    private final InterviewSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    @Tool("Compiles and runs Java, Python, or JavaScript code inside the sandboxed container and returns the test case results. Use this tool whenever the user asks to compile, execute, run, or verify their solution code.")
    public String executeCodeInSandbox(String language, String code) {
        Long sessionId = SessionContext.getCurrentSessionId();
        if (sessionId == null) {
            return "Error: No active interview session found in context.";
        }

        try {
            InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found with id: " + sessionId));

            String dsaProblemJson = session.getDsaProblemJson();
            if (dsaProblemJson == null || dsaProblemJson.isBlank()) {
                return "Error: No DSA problem has been assigned to this session yet.";
            }

            DsaProblem problem = objectMapper.readValue(dsaProblemJson, DsaProblem.class);
            
            ExecutionResult result = executionService.execute(
                language, 
                code, 
                problem.getTestCases() != null ? problem.getTestCases() : Collections.emptyList(),
                problem.getInputFormat(),
                problem.getOutputFormat(),
                problem.getMethodSignature()
            );

            StringBuilder feedback = new StringBuilder();
            feedback.append("Execution Status:\n");
            feedback.append("- Compiled successfully: ").append(result.isCompiled()).append("\n");
            if (!result.isCompiled()) {
                feedback.append("- Compilation Error Details: ").append(result.getCompileError()).append("\n");
            } else {
                feedback.append("- Total Test Cases Run: ").append(result.getTotalCount()).append("\n");
                feedback.append("- Test Cases Passed: ").append(result.getPassedCount()).append("\n");
                feedback.append("- All Test Cases Passed: ").append(result.isAllPassed()).append("\n");
                if (result.getResults() != null && !result.getResults().isEmpty()) {
                    feedback.append("- Details:\n");
                    for (int i = 0; i < result.getResults().size(); i++) {
                        var tcRes = result.getResults().get(i);
                        feedback.append(String.format("  * Test Case %d: %s (%d ms)\n", 
                            i + 1, tcRes.getStatus(), tcRes.getExecutionTimeMs()));
                        if (!tcRes.isPassed()) {
                            feedback.append("    Expected: ").append(tcRes.getExpectedOutput()).append("\n");
                            feedback.append("    Actual: ").append(tcRes.getActualOutput()).append("\n");
                            if (tcRes.getError() != null && !tcRes.getError().isBlank()) {
                                feedback.append("    Error details: ").append(tcRes.getError()).append("\n");
                            }
                        }
                    }
                }
            }
            return feedback.toString();
        } catch (Exception e) {
            log.error("Failed to execute code in tool sandbox for session {}", sessionId, e);
            return "Execution failed due to server/sandbox error: " + e.getMessage();
        }
    }
}
