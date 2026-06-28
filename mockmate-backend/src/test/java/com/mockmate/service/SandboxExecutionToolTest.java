package com.mockmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockmate.agent.tool.SandboxExecutionTool;
import com.mockmate.dto.code.DsaProblem;
import com.mockmate.dto.code.ExecutionResult;
import com.mockmate.model.InterviewSession;
import com.mockmate.repository.InterviewSessionRepository;
import com.mockmate.service.context.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class SandboxExecutionToolTest {

    @Mock
    private CodeExecutionService executionService;

    @Mock
    private InterviewSessionRepository sessionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SandboxExecutionTool sandboxExecutionTool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sandboxExecutionTool = new SandboxExecutionTool(
                executionService,
                sessionRepository,
                objectMapper
        );
    }

    @AfterEach
    void tearDown() {
        SessionContext.clear();
    }

    @Test
    void testExecuteCodeWithoutActiveSession() {
        String result = sandboxExecutionTool.executeCodeInSandbox("JAVA", "class Main {}");
        assertTrue(result.contains("Error: No active interview session found"));
    }

    @Test
    void testExecuteCodeWithMissingSession() {
        SessionContext.setCurrentSessionId(1L);
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        String result = sandboxExecutionTool.executeCodeInSandbox("JAVA", "class Main {}");
        assertTrue(result.contains("Execution failed"));
    }

    @Test
    void testExecuteCodeWithMissingDsaProblem() {
        SessionContext.setCurrentSessionId(1L);
        InterviewSession session = new InterviewSession();
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        String result = sandboxExecutionTool.executeCodeInSandbox("JAVA", "class Main {}");
        assertTrue(result.contains("Error: No DSA problem has been assigned"));
    }

    @Test
    void testExecuteCodeSuccess() throws Exception {
        SessionContext.setCurrentSessionId(1L);
        InterviewSession session = new InterviewSession();
        
        DsaProblem problem = new DsaProblem();
        problem.setTestCases(Collections.emptyList());
        problem.setInputFormat("text");
        problem.setOutputFormat("text");
        problem.setMethodSignature("main");
        session.setDsaProblemJson(objectMapper.writeValueAsString(problem));

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        ExecutionResult execResult = new ExecutionResult();
        execResult.setCompiled(true);
        execResult.setTotalCount(0);
        execResult.setPassedCount(0);
        execResult.setAllPassed(true);
        execResult.setResults(Collections.emptyList());

        when(executionService.execute(anyString(), anyString(), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(execResult);

        String result = sandboxExecutionTool.executeCodeInSandbox("JAVA", "class Main {}");
        assertTrue(result.contains("Execution Status"));
        assertTrue(result.contains("Compiled successfully: true"));
    }
}
