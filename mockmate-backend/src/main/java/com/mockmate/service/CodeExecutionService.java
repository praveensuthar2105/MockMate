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

    public ExecutionResult execute(String language, String code, List<TestCase> testCases, String inputFormat, String outputFormat, String methodSignature) {
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
                Path mainFile = tempDir.resolve("Main.java");
                Files.writeString(mainFile, code);

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
                    result.setCompileError(compileOutput.isEmpty() ? "Compilation failed or timed out." : compileOutput.replace("/code/Main.java:", "Line ").replace("Main.java:", "Line "));
                    return result;
                }
            } else if ("PYTHON".equalsIgnoreCase(language)) {
                Path sourceFile = tempDir.resolve("solution.py");
                Files.writeString(sourceFile, code);
            } else if ("JAVASCRIPT".equalsIgnoreCase(language) || "JS".equalsIgnoreCase(language)) {
                Path sourceFile = tempDir.resolve("solution.js");
                Files.writeString(sourceFile, code);
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
            if (e instanceof java.io.IOException && e.getMessage().contains("docker")) {
                result.setCompileError("Code execution unavailable. Docker is not running.");
            } else {
                result.setCompileError("Execution system error: " + e.getMessage());
            }
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
        result.setHidden(testCase.isHidden());
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
            } else if ("PYTHON".equalsIgnoreCase(language)) {
                command.addAll(List.of("python3", "/code/solution.py"));
            } else if ("JAVASCRIPT".equalsIgnoreCase(language) || "JS".equalsIgnoreCase(language)) {
                command.addAll(List.of("node", "/code/solution.js"));
            }

            Path inputPath = tempDir.resolve("input.txt");
            String rawInput = testCase.getInput() != null ? testCase.getInput() : "";
            Files.writeString(inputPath, rawInput.replace("\\n", "\n"));

            Path outputPath = tempDir.resolve("out.txt");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectInput(inputPath.toFile());
            pb.redirectOutput(outputPath.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            long executionTimeMs = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTimeMs);

            if (!finished) {
                process.destroyForcibly();
                result.setTimedOut(true);
                result.setError("Time Limit Exceeded");
                result.setStatus("Time Limit Exceeded");
                result.setPassed(false);
                return result;
            }

            String output = Files.readString(outputPath).trim();
            int exitCode = process.exitValue();
            result.setActualOutput(output);

            if (exitCode != 0) {
                result.setError(output);
                result.setStatus("Runtime Error");
                result.setPassed(false);
            } else {
                boolean matched = compareOutputs(output, testCase.getExpectedOutput());
                result.setPassed(matched);
                result.setStatus(matched ? "Accepted" : "Wrong Answer");
            }

        } catch (Exception e) {
            log.error("Failed to run test case", e);
            if (e instanceof java.io.IOException && e.getMessage() != null && e.getMessage().contains("Cannot run program \"docker\"")) {
                result.setError("Code execution unavailable. Docker is not running.");
                result.setStatus("Runtime Error");
            } else {
                result.setError("System error executing test case: " + e.getMessage());
                result.setStatus("Runtime Error");
            }
            result.setPassed(false);
        }

        return result;
    }

    private TestCaseResult runTestCaseLocally(String language, Path tempDir, TestCase testCase) {
        TestCaseResult result = new TestCaseResult();
        result.setInput(testCase.getInput());
        result.setExpectedOutput(testCase.getExpectedOutput());
        result.setHidden(testCase.isHidden());
        long startTime = System.currentTimeMillis();

        try {
            List<String> command = new ArrayList<>();
            if ("JAVA".equalsIgnoreCase(language)) {
                String javaHome = System.getProperty("java.home");
                String os = System.getProperty("os.name").toLowerCase();
                String exeSuffix = os.contains("win") ? ".exe" : "";
                String javaPath = javaHome + File.separator + "bin" + File.separator + "java" + exeSuffix;
                command.addAll(List.of(javaPath, "-cp", tempDir.toAbsolutePath().toString(), "Main"));
            } else if ("PYTHON".equalsIgnoreCase(language)) {
                command.addAll(List.of("python", tempDir.resolve("solution.py").toAbsolutePath().toString()));
            } else if ("JAVASCRIPT".equalsIgnoreCase(language) || "JS".equalsIgnoreCase(language)) {
                command.addAll(List.of("node", tempDir.resolve("solution.js").toAbsolutePath().toString()));
            }

            Path inputPath = tempDir.resolve("input.txt");
            String rawInput = testCase.getInput() != null ? testCase.getInput() : "";
            Files.writeString(inputPath, rawInput.replace("\\n", "\n"));

            Path outputPath = tempDir.resolve("out.txt");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectInput(inputPath.toFile());
            pb.redirectOutput(outputPath.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            long executionTimeMs = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTimeMs);

            if (!finished) {
                process.destroyForcibly();
                result.setTimedOut(true);
                result.setError("Time Limit Exceeded (Local)");
                result.setStatus("Time Limit Exceeded");
                result.setPassed(false);
                return result;
            }

            String output = Files.readString(outputPath).trim();
            int exitCode = process.exitValue();
            result.setActualOutput(output);

            if (exitCode != 0) {
                result.setError(output);
                result.setStatus("Runtime Error");
                result.setPassed(false);
            } else {
                boolean matched = compareOutputs(output, testCase.getExpectedOutput());
                result.setPassed(matched);
                result.setStatus(matched ? "Accepted" : "Wrong Answer");
            }

        } catch (Exception e) {
            log.error("Failed to run local test case", e);
            result.setError("Local system error: " + e.getMessage());
            result.setStatus("Runtime Error");
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
        String normActual = normalizeOutput(actual);
        String normExpected = normalizeOutput(expected);
        return normActual.equals(normExpected);
    }

    private String normalizeOutput(String val) {
        if (val == null) {
            return "";
        }
        String normalized = val.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = normalized.split("\n", -1);
        List<String> cleanedLines = new ArrayList<>();
        for (String line : lines) {
            cleanedLines.add(line.stripTrailing());
        }
        int lastNonEmptyIndex = cleanedLines.size() - 1;
        while (lastNonEmptyIndex >= 0 && cleanedLines.get(lastNonEmptyIndex).isEmpty()) {
            lastNonEmptyIndex--;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= lastNonEmptyIndex; i++) {
            sb.append(cleanedLines.get(i));
            if (i < lastNonEmptyIndex) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
