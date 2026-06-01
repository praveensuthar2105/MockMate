package com.mockmate.service;

import com.mockmate.dto.code.ExecutionResult;
import com.mockmate.dto.code.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeExecutionServiceTest {

    private CodeExecutionService codeExecutionService;

    @BeforeEach
    void setUp() {
        codeExecutionService = new CodeExecutionService();
    }

    private TestCase createTestCase(String input, String expectedOutput) {
        TestCase tc = new TestCase();
        tc.setInput(input);
        tc.setExpectedOutput(expectedOutput);
        return tc;
    }

    @Test
    void testJavaIntArrayFormat_Correct() {
        String code = "import java.util.*;\n" +
                      "public class Main {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        Scanner sc = new Scanner(System.in);\n" +
                      "        if (sc.hasNextLine()) {\n" +
                      "            String line = sc.nextLine();\n" +
                      "            int target = sc.nextInt();\n" +
                      "            System.out.println(\"0 1\");\n" +
                      "        }\n" +
                      "    }\n" +
                      "}";
        List<TestCase> testCases = List.of(createTestCase("2 7 11 15\n9", "0 1"));

        ExecutionResult result = codeExecutionService.execute("JAVA", code, testCases, "", "", "");

        assertTrue(result.isCompiled());
        assertNull(result.getCompileError());
        assertTrue(result.isAllPassed());
    }

    @Test
    void testPythonIntArrayFormat_Correct() {
        String code = "import sys\n" +
                      "def main():\n" +
                      "    line1 = sys.stdin.readline()\n" +
                      "    line2 = sys.stdin.readline()\n" +
                      "    print(\"0 1\")\n" +
                      "if __name__ == '__main__':\n" +
                      "    main()";
        List<TestCase> testCases = List.of(createTestCase("2 7 11 15\n9", "0 1"));

        ExecutionResult result = codeExecutionService.execute("PYTHON", code, testCases, "", "", "");

        assertTrue(result.isCompiled());
        assertNull(result.getCompileError());
        assertTrue(result.isAllPassed());
    }

    @Test
    void testJavaCompilationError() {
        String code = "public class Main {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        System.out.println(\"Hello\") // missing semicolon\n" +
                      "    }\n" +
                      "}";
        List<TestCase> testCases = List.of(createTestCase("2 7 11 15\n9", "0 1"));

        ExecutionResult result = codeExecutionService.execute("JAVA", code, testCases, "", "", "");

        assertFalse(result.isCompiled());
        assertNotNull(result.getCompileError());
        assertTrue(result.getCompileError().contains("error: ';' expected"));
    }

    @Test
    void testJavaTimeout() {
        String code = "public class Main {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        while(true) {}\n" +
                      "    }\n" +
                      "}";
        List<TestCase> testCases = List.of(createTestCase("2 7 11 15\n9", "0 1"));

        ExecutionResult result = codeExecutionService.execute("JAVA", code, testCases, "", "", "");

        assertTrue(result.isCompiled());
        assertFalse(result.isAllPassed());
        assertTrue(result.getResults().get(0).isTimedOut());
    }

    @Test
    void testJavaRuntimeException() {
        String code = "public class Main {\n" +
                      "    public static void main(String[] args) {\n" +
                      "        int[] arr = new int[2];\n" +
                      "        int x = arr[100];\n" +
                      "    }\n" +
                      "}";
        List<TestCase> testCases = List.of(createTestCase("2 7", "0 1"));

        ExecutionResult result = codeExecutionService.execute("JAVA", code, testCases, "", "", "");

        assertTrue(result.isCompiled());
        assertFalse(result.isAllPassed());
        assertNotNull(result.getResults().get(0).getError());
        assertTrue(result.getResults().get(0).getError().contains("IndexOutOfBoundsException") || result.getResults().get(0).getError().contains("ArrayIndexOutOfBoundsException"));
    }

    @Test
    void testSmartRecoveryPythonMatrix() {
        String code = "import sys\n" +
                      "def main():\n" +
                      "    print(\"true\")\n" +
                      "if __name__ == '__main__':\n" +
                      "    main()";
        List<TestCase> testCases = List.of(createTestCase("1 3 5 7\n10 11 16 20\n3", "true"));

        ExecutionResult result = codeExecutionService.execute("PYTHON", code, testCases, "", "", "");

        assertTrue(result.isCompiled());
        assertTrue(result.isAllPassed());
    }
}
