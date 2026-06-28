package com.mockmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockmate.dto.code.DsaProblem;
import com.mockmate.dto.code.ProblemExample;
import com.mockmate.dto.code.ProblemHint;
import com.mockmate.dto.code.TestCase;
import com.mockmate.model.InterviewSession;
import com.mockmate.repository.InterviewSessionRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DsaProblemService {

    private final Optional<ChatLanguageModel> chatLanguageModel;
    private final InterviewSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    private String dsaProblemPromptTemplate;

    @PostConstruct
    public void loadPromptTemplates() {
        try {
            dsaProblemPromptTemplate = new ClassPathResource("prompts/dsa-problem-generation.txt")
                    .getContentAsString(StandardCharsets.UTF_8);
            log.info("DSA problem generation prompt template loaded successfully");
        } catch (IOException e) {
            log.error("Failed to load DSA problem generation prompt template", e);
            throw new RuntimeException("Failed to load prompt template: prompts/dsa-problem-generation.txt", e);
        }
    }

    public DsaProblem generateProblem(InterviewSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session must not be null");
        }
        return generateProblem(session.getId());
    }

    public DsaProblem generateProblem(Long sessionId) {
        InterviewSession session = sessionRepository.findByIdWithUser(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getDsaProblemJson() != null && !session.getDsaProblemJson().isEmpty()) {
            try {
                DsaProblem problem = objectMapper.readValue(session.getDsaProblemJson(), DsaProblem.class);
                sanitizeTestCasesAndExamples(problem);
                return problem;
            } catch (Exception e) {
                log.warn("Failed to parse existing DsaProblem from dsaProblemJson", e);
            }
        }

        if (chatLanguageModel.isEmpty()) {
            log.warn("Gemini API key is not configured. Using fallback DSA problem.");
            String difficulty = session.getDifficulty() != null ? session.getDifficulty().name() : "EASY";
            DsaProblem fallback = getFallbackProblem(difficulty);
            persistProblem(session.getId(), fallback);
            return fallback;
        }

        String company = session.getCompany();
        String difficulty = session.getDifficulty() != null ? session.getDifficulty().name() : "EASY";

        String prompt = dsaProblemPromptTemplate
                .replace("{difficulty}", difficulty)
                .replace("{company}", company);

        DsaProblem problem = null;

        List<InterviewSession> recentSessions = sessionRepository
                .findByUserIdOrderByStartedAtDesc(session.getUser().getId());

        for (int attempts = 0; attempts < 3; attempts++) {
            try {
                var request = ChatRequest.builder()
                        .messages(List.of(
                                SystemMessage.from(
                                        "You are an expert technical interviewer. Return ONLY valid, compact JSON. No markdown, no code fences, no explanations."),
                                UserMessage.from(prompt)))
                        .build();

                String response = chatLanguageModel.orElseThrow().chat(request).aiMessage().text();
                response = response.replaceAll("```json", "").replaceAll("```", "").trim();

                // Attempt to repair truncated JSON
                response = repairTruncatedJson(response);
                log.debug("Gemini response (attempt {}): {}", attempts + 1, response.substring(0, Math.min(200, response.length())));

                problem = objectMapper.readValue(response, DsaProblem.class);
                sanitizeTestCasesAndExamples(problem);

                // Auto-generate starter code from inputFormat/outputFormat/methodSignature
                generateStarterCode(problem);

                boolean duplicateFound = false;
                for (int i = 0; i < Math.min(5, recentSessions.size()); i++) {
                    InterviewSession pastSession = recentSessions.get(i);
                    if (!pastSession.getId().equals(session.getId()) && pastSession.getDsaProblemJson() != null) {
                        try {
                            DsaProblem pastProblem = objectMapper.readValue(pastSession.getDsaProblemJson(),
                                    DsaProblem.class);
                            if (pastProblem.getTitle() != null
                                    && pastProblem.getTitle().equalsIgnoreCase(problem.getTitle())) {
                                duplicateFound = true;
                                break;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                if (!duplicateFound) {
                    break;
                } else if (attempts == 1) {
                    log.warn("Duplicate problem title {} found again, using it anyway", problem.getTitle());
                } else {
                    log.info("Duplicate problem title {} found, retrying generation", problem.getTitle());
                    problem = null;
                }
            } catch (Exception e) {
                log.error("Failed to generate or parse problem from Gemini", e);
            }
        }

        if (problem == null) {
            log.info("Using fallback problem for difficulty {}", difficulty);
            problem = getFallbackProblem(difficulty);
        }

        sanitizeTestCasesAndExamples(problem);
        persistProblem(sessionId, problem);

        return problem;
    }

    @Transactional
    public void persistProblem(Long sessionId, DsaProblem problem) {
        InterviewSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        try {
            String json = objectMapper.writeValueAsString(problem);
            session.setDsaProblemJson(json);
            session.setDsaProblemGenerated(true);
            session.setDsaProblemGeneratedAt(java.time.LocalDateTime.now());
            sessionRepository.save(session);
        } catch (Exception e) {
            log.error("Failed to save DsaProblem to session", e);
        }
    }

    private DsaProblem getFallbackProblem(String difficulty) {
        DsaProblem problem = new DsaProblem();
        problem.setExamples(new ArrayList<>());
        problem.setDifficulty(difficulty);

        if ("HARD".equalsIgnoreCase(difficulty)) {
            problem.setTitle("Word Break II");
            problem.setDescription(
                    "Given a string s and a dictionary of strings wordDict, add spaces in s to construct a sentence where each word is a valid dictionary word. Return all such possible sentences in any order.");
            problem.setConstraints(List.of(
                    "1 <= s.length <= 20",
                    "1 <= wordDict.length <= 1000",
                    "1 <= wordDict[i].length <= 10",
                    "All strings in wordDict are unique"));
            problem.setTimeComplexityExpected("O(2^N)");
            problem.setSpaceComplexityExpected("O(2^N)");
            problem.setTestCases(List.of(
                    createTestCase("catsanddog\ncats,dog,sand,and,cat", "cats and dog\ncat sand dog"),
                    createTestCase("pineapplepenapple\napple,pen,applepen,pine,pineapple",
                            "pine apple pen apple\npineapple pen apple\npine applepen apple"),
                    createTestCase("catsandog\ncats,dog,sand,and,cat", "")));
            problem.setExamples(toExamples(problem.getTestCases()));
            problem.setHints(List.of(
                    createHint(1, "Think about how you can break this problem into smaller subproblems using recursion."),
                    createHint(2, "Use memoization to avoid recomputing results for the same substring."),
                    createHint(3, "Use backtracking: for each prefix that matches a word, recursively solve for the remaining suffix.")));
            problem.setInputFormat("First line contains the string s. Second line contains comma-separated words representing the dictionary wordDict.");
            problem.setOutputFormat("Print all possible sentences where each word is a valid dictionary word, one per line. If no sentence is possible, print nothing.");
            problem.setMethodSignature("wordBreak");
        } else if ("MEDIUM".equalsIgnoreCase(difficulty)) {
            problem.setTitle("Longest Substring Without Repeating Characters");
            problem.setDescription(
                    "Given a string s, find the length of the longest substring without repeating characters.");
            problem.setConstraints(List.of(
                    "0 <= s.length <= 5 * 10^4",
                    "s consists of English letters, digits, symbols and spaces"));
            problem.setTimeComplexityExpected("O(N)");
            problem.setSpaceComplexityExpected("O(min(N, M))");
            problem.setTestCases(List.of(
                    createTestCase("abcabcbb", "3"),
                    createTestCase("bbbbb", "1"),
                    createTestCase("pwwkew", "3")));
            problem.setExamples(toExamples(problem.getTestCases()));
            problem.setHints(List.of(
                    createHint(1, "Consider using a sliding window approach with two pointers."),
                    createHint(2, "Use a HashSet or HashMap to track characters in the current window."),
                    createHint(3, "When a duplicate is found, shrink the window from the left until the duplicate is removed.")));
            problem.setInputFormat("A single line containing the string s.");
            problem.setOutputFormat("Print a single integer representing the length of the longest substring without repeating characters.");
            problem.setMethodSignature("lengthOfLongestSubstring");
        } else {
            problem.setTitle("Two Sum");
            problem.setDescription(
                    "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target. You may assume that each input would have exactly one solution.");
            problem.setConstraints(List.of(
                    "2 <= nums.length <= 10^4",
                    "-10^9 <= nums[i] <= 10^9",
                    "-10^9 <= target <= 10^9",
                    "Only one valid answer exists"));
            problem.setTimeComplexityExpected("O(N)");
            problem.setSpaceComplexityExpected("O(N)");
            problem.setTestCases(List.of(
                    createTestCase("2 7 11 15\n9", "0 1"),
                    createTestCase("3 2 4\n6", "1 2"),
                    createTestCase("3 3\n6", "0 1")));
            problem.setExamples(toExamples(problem.getTestCases()));
            problem.setHints(List.of(
                    createHint(1, "A brute force approach uses two nested loops — can you do better?"),
                    createHint(2, "Use a HashMap to store each number's complement as you iterate."),
                    createHint(3, "For each element, check if (target - nums[i]) already exists in the map.")));
            problem.setInputFormat("First line contains space-separated integers representing the nums array. Second line contains a single integer representing the target.");
            problem.setOutputFormat("Print two space-separated integers representing the indices of the two numbers.");
            problem.setMethodSignature("twoSum");
        }

        generateStarterCode(problem);
        return problem;
    }

    private List<ProblemExample> toExamples(List<TestCase> testCases) {
        return testCases.stream().map(tc -> {
            ProblemExample ex = new ProblemExample();
            ex.setInput(tc.getInput());
            ex.setOutput(tc.getExpectedOutput());
            ex.setExplanation("Basic test case evaluation.");
            return ex;
        }).toList();
    }

    private TestCase createTestCase(String input, String output) {
        TestCase tc = new TestCase();
        tc.setInput(input);
        tc.setExpectedOutput(output);
        return tc;
    }

    private ProblemHint createHint(int level, String hint) {
        ProblemHint ph = new ProblemHint();
        ph.setLevel(level);
        ph.setHint(hint);
        return ph;
    }

    /**
     * Auto-generates starter code for Java, Python, and JavaScript for Mock Interview (Scanner/stdin/stdout) format.
     */
    private void generateStarterCode(DsaProblem problem) {
        problem.setJavaStarterCode(
                "import java.util.*;\nimport java.io.*;\n\npublic class Main {\n    public static void main(String[] args) throws IOException {\n        Scanner sc = new Scanner(System.in);\n        // Write your solution here\n        \n    }\n}");
        problem.setPythonStarterCode(
                "import sys\n\ndef main():\n    # Write your solution here\n    pass\n\nif __name__ == '__main__':\n    main()");
        problem.setJavascriptStarterCode(
                "const fs = require('fs');\n\nfunction main() {\n    const input = fs.readFileSync(0, 'utf-8');\n    // Write your solution here\n    \n}\n\nmain();");
    }

    /**
     * Attempts to repair truncated JSON by finding the longest prefix that can be cleanly closed.
     */
    String repairTruncatedJson(String json) {
        if (json == null || json.isEmpty()) return json;

        // Check if it already parses cleanly
        try {
            objectMapper.readTree(json);
            return json; // valid JSON
        } catch (Exception ignored) {
            // needs repair
        }

        // Search backward from the end, looking for the longest prefix that can be closed
        int minLen = Math.max(1, json.length() - 2000);
        for (int len = json.length(); len >= minLen; len--) {
            String prefix = json.substring(0, len);
            String repaired = tryCloseJson(prefix);
            if (repaired != null) {
                log.debug("JSON repair applied: trimmed to length {} and successfully closed", repaired.length());
                return repaired;
            }
        }

        // Final fallback: original naive logic to append closing brackets/braces
        return naiveRepairJson(json);
    }

    String tryCloseJson(String prefix) {
        StringBuilder sb = new StringBuilder(prefix);
        boolean inString = false;
        boolean escaped = false;
        java.util.Deque<Character> stack = new java.util.ArrayDeque<>();

        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (c == '{') stack.push('}');
            else if (c == '[') stack.push(']');
            else if (c == '}' || c == ']') {
                if (!stack.isEmpty()) {
                    char expected = stack.peek();
                    if ((c == '}' && expected == '}') || (c == ']' && expected == ']')) {
                        stack.pop();
                    } else {
                        return null; // Mismatched brackets in the prefix itself
                    }
                } else {
                    return null; // Extra closing brackets
                }
            }
        }

        if (!inString) {
            String trimmed = prefix.trim();
            if (trimmed.endsWith(":") || trimmed.endsWith(",") || trimmed.endsWith("{") || trimmed.endsWith("[")) {
                return null; // Avoid trailing colons, commas, or empty/unopened structures
            }
        }

        if (inString) {
            // Find where the current string started (the last unescaped double quote)
            int lastQuote = -1;
            boolean esc = false;
            for (int i = 0; i < prefix.length(); i++) {
                char c = prefix.charAt(i);
                if (esc) {
                    esc = false;
                    continue;
                }
                if (c == '\\') {
                    esc = true;
                    continue;
                }
                if (c == '"') {
                    lastQuote = i;
                }
            }
            if (lastQuote != -1) {
                String beforeQuote = prefix.substring(0, lastQuote).trim();
                if (beforeQuote.endsWith(",") || beforeQuote.endsWith(":") || beforeQuote.endsWith("{") || beforeQuote.endsWith("[")) {
                    if (prefix.substring(lastQuote + 1).trim().isEmpty()) {
                        return null; // Reject empty strings created by truncation immediately after comma/colon/bracket
                    }
                }
            }
            sb.append('"');
        }

        while (!stack.isEmpty()) {
            sb.append(stack.pop());
        }

        String candidate = sb.toString();
        try {
            objectMapper.readTree(candidate);
            return candidate;
        } catch (Exception e) {
            return null;
        }
    }

    private String naiveRepairJson(String json) {
        StringBuilder sb = new StringBuilder(json);
        boolean inString = false;
        boolean escaped = false;
        java.util.Deque<Character> stack = new java.util.ArrayDeque<>();

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (c == '{') stack.push('}');
            else if (c == '[') stack.push(']');
            else if (c == '}' || c == ']') {
                if (!stack.isEmpty()) stack.pop();
            }
        }

        if (inString) {
            sb.append('"');
        }

        while (!stack.isEmpty()) {
            sb.append(stack.pop());
        }

        return sb.toString();
    }

    private void sanitizeTestCasesAndExamples(DsaProblem problem) {
        if (problem != null && problem.getTestCases() != null) {
            for (int i = 0; i < problem.getTestCases().size(); i++) {
                problem.getTestCases().get(i).setHidden(i >= 3);
            }
            // Enforce that examples only contains the first 3 visible test cases
            List<TestCase> visibleCases = problem.getTestCases().stream()
                    .filter(tc -> !tc.isHidden())
                    .toList();
            problem.setExamples(toExamples(visibleCases));
        }
    }
}
