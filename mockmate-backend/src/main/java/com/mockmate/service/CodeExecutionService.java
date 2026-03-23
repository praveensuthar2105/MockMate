package com.mockmate.service;

import com.mockmate.dto.code.ExecutionResult;
import com.mockmate.dto.code.TestCase;
import com.mockmate.dto.code.TestCaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CodeExecutionService {

    public ExecutionResult execute(String language, String code, List<TestCase> testCases, String testRunner) {
        ExecutionResult result = new ExecutionResult();
        result.setCompiled(true);
        result.setResults(new ArrayList<>());

        boolean useDocker = isDockerRunning();
        if (!useDocker) {
            log.warn("Docker is not running. Falling back to LOCAL execution (security risk).");
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("mockmate-exec-");

            if ("JAVA".equalsIgnoreCase(language)) {
                if (testRunner != null && !testRunner.isEmpty()) {
                    Path solutionFile = tempDir.resolve("Solution.java");
                    Files.writeString(solutionFile, code);
                    Path mainFile = tempDir.resolve("Main.java");
                    Files.writeString(mainFile, testRunner);
                } else {
                    Path sourceFile = tempDir.resolve("Main.java");
                    Files.writeString(sourceFile, code);
                }

                String javaHome = System.getProperty("java.home");
                String os = System.getProperty("os.name").toLowerCase();
                String exeSuffix = os.contains("win") ? ".exe" : "";
                String javacPath = javaHome + File.separator + "bin" + File.separator + "javac" + exeSuffix;
                ProcessBuilder pb = new ProcessBuilder(javacPath, "Main.java");
                pb.directory(tempDir.toFile());
                pb.redirectErrorStream(true);
                Process compileProcess = pb.start();

                java.util.concurrent.CompletableFuture<String> compileOutputFuture = java.util.concurrent.CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return new String(compileProcess.getInputStream().readAllBytes());
                            } catch (Exception e) {
                                return "";
                            }
                        });

                boolean compiled = compileProcess.waitFor(5, TimeUnit.SECONDS);

                if (!compiled) {
                    compileProcess.destroyForcibly();
                }
                String compileOutput = compileOutputFuture.get();

                if (!compiled || compileProcess.exitValue() != 0) {
                    if (compileProcess.isAlive()) {
                        compileProcess.destroyForcibly();
                    }
                    result.setCompiled(false);
                    result.setCompileError(
                            compileOutput.isEmpty() ? "Compilation failed or timed out." : compileOutput);
                    return result;
                }
            } else if ("PYTHON".equalsIgnoreCase(language)) {
                String finalCode = code;
                if (testRunner != null && !testRunner.isEmpty()) {
                    finalCode = code + "\n\n" + testRunner;
                }
                Path sourceFile = tempDir.resolve("solution.py");
                Files.writeString(sourceFile, finalCode);
            } else {
                result.setCompiled(false);
                result.setCompileError("Unsupported language: " + language);
                return result;
            }

            int passedCount = 0;
            for (TestCase tc : testCases) {
                TestCaseResult tcResult = useDocker
                        ? runTestCase(language, tempDir, tc)
                        : runTestCaseLocally(language, tempDir, tc);
                result.getResults().add(tcResult);
                if (tcResult.isPassed()) {
                    passedCount++;
                }
            }

            result.setPassedCount(passedCount);
            result.setTotalCount(testCases.size());
            result.setAllPassed(passedCount > 0 && passedCount == testCases.size());

        } catch (Exception e) {
            log.error("Execution error", e);
            result.setCompiled(false);
            result.setCompileError("Execution system error: " + e.getMessage());
        } finally {
            if (tempDir != null) {
                deleteDirectory(tempDir.toFile());
            }
        }

        return result;
    }

    private TestCaseResult runTestCase(String language, Path tempDir, TestCase testCase) {
        TestCaseResult result = new TestCaseResult();
        result.setInput(testCase.getInput());
        result.setExpectedOutput(testCase.getExpectedOutput());
        long startTime = System.currentTimeMillis();

        try {
            List<String> command = new ArrayList<>(List.of(
                    "docker", "run", "--rm",
                    "--network", "none",
                    "--memory", "256m",
                    "--cpus", "0.5",
                    "--read-only",
                    "--tmpfs", "/tmp:size=10m",
                    "-i",
                    "-v", tempDir.toAbsolutePath().toString() + ":/code:ro",
                    "mockmate-sandbox"));

            if ("JAVA".equalsIgnoreCase(language)) {
                command.addAll(List.of("java", "-cp", "/code", "Main"));
            } else {
                command.addAll(List.of("python3", "/code/solution.py"));
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            if (testCase.getInput() != null && !testCase.getInput().isEmpty()) {
                try (java.io.OutputStream out = process.getOutputStream()) {
                    out.write(testCase.getInput().getBytes());
                    out.flush();
                }
            } else {
                process.getOutputStream().close();
            }

            java.util.concurrent.CompletableFuture<String> outputFuture = java.util.concurrent.CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            return new String(process.getInputStream().readAllBytes());
                        } catch (Exception e) {
                            return "";
                        }
                    });

            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            long executionTimeMs = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTimeMs);

            if (!finished) {
                process.destroyForcibly();
            }
            String output = outputFuture.get().trim();

            if (!finished) {
                result.setTimedOut(true);
                result.setError("Time Limit Exceeded");
                result.setPassed(false);
                return result;
            }

            int exitCode = process.exitValue();
            result.setActualOutput(output);

            String expectedTrimmed = testCase.getExpectedOutput() != null ? testCase.getExpectedOutput().trim() : "";

            if (exitCode != 0) {
                result.setError(output);
                result.setPassed(false);
            } else {
                result.setPassed(compareOutputs(output, expectedTrimmed));
            }

        } catch (Exception e) {
            log.error("Failed to run test case", e);
            result.setError("System error executing test case");
            result.setPassed(false);
        }

        return result;
    }

    private TestCaseResult runTestCaseLocally(String language, Path tempDir, TestCase testCase) {
        TestCaseResult result = new TestCaseResult();
        result.setInput(testCase.getInput());
        result.setExpectedOutput(testCase.getExpectedOutput());
        long startTime = System.currentTimeMillis();

        try {
            List<String> command = new ArrayList<>();
            if ("JAVA".equalsIgnoreCase(language)) {
                // Use current java bin
                String javaHome = System.getProperty("java.home");
                String os = System.getProperty("os.name").toLowerCase();
                String exeSuffix = os.contains("win") ? ".exe" : "";
                String javaPath = javaHome + File.separator + "bin" + File.separator + "java" + exeSuffix;
                command.addAll(List.of(javaPath, "-cp", tempDir.toAbsolutePath().toString(), "Main"));
            } else {
                command.addAll(List.of("python", tempDir.resolve("solution.py").toAbsolutePath().toString()));
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            if (testCase.getInput() != null && !testCase.getInput().isEmpty()) {
                try (java.io.OutputStream out = process.getOutputStream()) {
                    out.write(testCase.getInput().getBytes());
                    out.flush();
                }
            } else {
                process.getOutputStream().close();
            }

            java.util.concurrent.CompletableFuture<String> outputFuture = java.util.concurrent.CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            return new String(process.getInputStream().readAllBytes());
                        } catch (Exception e) {
                            return "";
                        }
                    });

            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            long executionTimeMs = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTimeMs);

            if (!finished) {
                process.destroyForcibly();
                result.setTimedOut(true);
                result.setError("Time Limit Exceeded (Local)");
                result.setPassed(false);
                return result;
            }

            String output = outputFuture.get().trim();
            int exitCode = process.exitValue();
            result.setActualOutput(output);

            String expectedTrimmed = testCase.getExpectedOutput() != null ? testCase.getExpectedOutput().trim() : "";

            if (exitCode != 0) {
                result.setError(output);
                result.setPassed(false);
            } else {
                result.setPassed(compareOutputs(output, expectedTrimmed));
            }

        } catch (Exception e) {
            log.error("Failed to run local test case", e);
            result.setError("Local system error: " + e.getMessage());
            result.setPassed(false);
        }

        return result;
    }

    private boolean isDockerRunning() {
        String useDockerEnv = System.getenv("USE_DOCKER");
        if (useDockerEnv != null && "false".equalsIgnoreCase(useDockerEnv)) {
            log.info("Docker sandbox explicitly disabled via USE_DOCKER env var.");
            return false;
        }
        try {
            Process process = new ProcessBuilder("docker", "info").start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("Docker info check timed out after 5s.");
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.warn("Docker info returned non-zero exit code: {}", exitCode);
            }
            return exitCode == 0;
        } catch (Exception e) {
            log.warn("Docker info check failed: {}", e.getMessage());
            return false;
        }
    }

    private void deleteDirectory(File file) {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        file.delete();
    }

    private boolean compareOutputs(String actual, String expected) {
        if (actual == null || expected == null) {
            return java.util.Objects.equals(actual, expected);
        }

        String a = actual.trim();
        String e = expected.trim();

        if (a.equals(e)) {
            return true;
        }

        // Handle multi-line results where order might not matter (e.g. Word Break II)
        String[] aLines = a.split("\\r?\\n");
        String[] eLines = e.split("\\r?\\n");

        if (aLines.length != eLines.length) {
            return false;
        }

        if (aLines.length <= 1) {
            return false; // Single line didn't match via strict equals above
        }

        List<String> aList = new ArrayList<>();
        for (String s : aLines) {
            if (!s.trim().isEmpty())
                aList.add(s.trim());
        }
        java.util.Collections.sort(aList);

        List<String> eList = new ArrayList<>();
        for (String s : eLines) {
            if (!s.trim().isEmpty())
                eList.add(s.trim());
        }
        java.util.Collections.sort(eList);

        return aList.equals(eList);
    }
}
