package com.mockmate.dto.code;

import lombok.Data;

@Data
public class TestCaseResult {
    private boolean passed;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private String error;
    private boolean timedOut;
    private long executionTimeMs;
}
