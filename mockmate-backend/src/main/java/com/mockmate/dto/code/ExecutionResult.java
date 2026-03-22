package com.mockmate.dto.code;

import lombok.Data;
import java.util.List;

@Data
public class ExecutionResult {
    private boolean compiled;
    private String compileError;
    private List<TestCaseResult> results;
    private int passedCount;
    private int totalCount;
    private boolean allPassed;
}
