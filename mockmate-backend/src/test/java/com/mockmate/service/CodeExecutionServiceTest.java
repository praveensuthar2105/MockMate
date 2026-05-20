package com.mockmate.service;

import com.mockmate.dto.code.ExecutionResult;
import com.mockmate.dto.code.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeExecutionServiceTest {

    private CodeExecutionService codeExecutionService;
    private RunnerTemplateService runnerTemplateService;

    @BeforeEach
    void setUp() {
        runnerTemplateService = new RunnerTemplateService();
        codeExecutionService = new CodeExecutionService(runnerTemplateService);
    }

    private TestCase createTestCase(String input, String expectedOutput) {
        TestCase tc = new TestCase();
        tc.setInput(input);
        tc.setExpectedOutput(expectedOutput);
        return tc;
    }

    @Test
    void testJavaIntArrayFormat_Correct() {
        String code = "class Solution { public int[] twoSum(int[] nums, int target) { return new int[]{0,1}; } }";
        List<TestCase> testCases = List.of(createTestCase("[2,7,11,15] 9", "[0, 1]"));

        ExecutionResult result = codeExecutionService.execute("JAVA", code, testCases, "int_array+int", "int_array", "twoSum");

        assertTrue(result.isCompiled());
        assertNull(result.getCompileError());
        assertTrue(result.isAllPassed());
    }

    @Test
    void testPythonIntArrayFormat_Correct() {
        String code = "class Solution:\n    def twoSum(self, nums, target):\n        return [0, 1]";
        List<TestCase> testCases = List.of(createTestCase("[2,7,11,15] 9", "[0, 1]"));

        ExecutionResult result = codeExecutionService.execute("PYTHON", code, testCases, "int_array+int", "int_array", "twoSum");

        assertTrue(result.isCompiled());
        assertNull(result.getCompileError());
        assertTrue(result.isAllPassed());
    }

    @Test
    void testJavaCompilationError() {
        String code = "class Solution { public int[] twoSum(int[] nums, int target) { return new int[]{0,1} } }"; // missing semicolon
        List<TestCase> testCases = List.of(createTestCase("[2,7,11,15] 9", "[0, 1]"));

        ExecutionResult result = codeExecutionService.execute("JAVA", code, testCases, "int_array+int", "int_array", "twoSum");

        assertFalse(result.isCompiled());
        assertNotNull(result.getCompileError());
        assertTrue(result.getCompileError().contains("error: ';' expected"));
    }

    @Test
    void testJavaTimeout() {
        String code = "class Solution { public int[] twoSum(int[] nums, int target) { while(true){} } }";
        List<TestCase> testCases = List.of(createTestCase("[2,7,11,15] 9", "[0, 1]"));

        ExecutionResult result = codeExecutionService.execute("JAVA", code, testCases, "int_array+int", "int_array", "twoSum");

        assertTrue(result.isCompiled());
        assertFalse(result.isAllPassed());
        assertTrue(result.getResults().get(0).isTimedOut());
    }

    @Test
    void testJavaRuntimeException() {
        String code = "class Solution { public int[] twoSum(int[] nums, int target) { int x = nums[100]; return new int[]{x}; } }";
        List<TestCase> testCases = List.of(createTestCase("[2,7] 9", "[0, 1]"));

        ExecutionResult result = codeExecutionService.execute("JAVA", code, testCases, "int_array+int", "int_array", "twoSum");

        assertTrue(result.isCompiled());
        assertFalse(result.isAllPassed());
        assertNotNull(result.getResults().get(0).getError());
        assertTrue(result.getResults().get(0).getError().contains("IndexOutOfBoundsException") || result.getResults().get(0).getError().contains("ArrayIndexOutOfBoundsException"));
    }

    @Test
    void testSmartRecoveryPythonMatrix() {
        String code = "class Solution:\n    def searchMatrix(self, matrix, target):\n        return True";
        // User signature says int_array+int, but code uses List[List[int]] format (in smart recovery logic it checks for specific strings, let's inject it into code so it hits it)
        String smartCode = "class Solution:\n    def searchMatrix(self, matrix: List[List[int]], target: int):\n        return True";
        List<TestCase> testCases = List.of(createTestCase("[[1,3,5,7],[10,11,16,20],[23,30,34,60]] 3", "true"));

        ExecutionResult result = codeExecutionService.execute("PYTHON", smartCode, testCases, "int_array+int", "boolean", "searchMatrix");

        assertTrue(result.isCompiled());
        assertTrue(result.isAllPassed());
    }
}
