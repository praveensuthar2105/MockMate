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

    public DsaProblem generateProblem(Long sessionId) {
        InterviewSession session = sessionRepository.findByIdWithUser(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getDsaProblemJson() != null && !session.getDsaProblemJson().isEmpty()) {
            try {
                return objectMapper.readValue(session.getDsaProblemJson(), DsaProblem.class);
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

                // Auto-populate examples from testCases if not provided
                if ((problem.getExamples() == null || problem.getExamples().isEmpty())
                        && problem.getTestCases() != null && !problem.getTestCases().isEmpty()) {
                    problem.setExamples(toExamples(problem.getTestCases()));
                }

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
            problem.setInputFormat("string, string_array");
            problem.setOutputFormat("string_array");
            problem.setMethodSignature("wordBreak");
            problem.setJavaStarterCode(
                    "class Solution {\n    public List<String> wordBreak(String s, List<String> wordDict) {\n        \n    }\n}");
            problem.setPythonStarterCode(
                    "class Solution:\n    def wordBreak(self, s: str, wordDict: List[str]) -> List[str]:\n        pass");
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
            problem.setInputFormat("string");
            problem.setOutputFormat("int");
            problem.setMethodSignature("lengthOfLongestSubstring");
            problem.setJavaStarterCode(
                    "class Solution {\n    public int lengthOfLongestSubstring(String s) {\n        \n    }\n}");
            problem.setPythonStarterCode(
                    "class Solution:\n    def lengthOfLongestSubstring(self, s: str) -> int:\n        pass");
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
            problem.setInputFormat("int_array+int");
            problem.setOutputFormat("int_array");
            problem.setMethodSignature("twoSum");
            problem.setJavaStarterCode(
                    "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        \n    }\n}");
            problem.setPythonStarterCode(
                    "class Solution:\n    def twoSum(self, nums: List[int], target: int) -> List[int]:\n        pass");
        }

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
     * Auto-generates starter code for Java, Python, and JavaScript from inputFormat/outputFormat/methodSignature.
     */
    private void generateStarterCode(DsaProblem problem) {
        String method = problem.getMethodSignature();
        String input = problem.getInputFormat();
        String output = problem.getOutputFormat();
        if (method == null || input == null || output == null) return;

        // Only generate if not already present
        if (problem.getJavaStarterCode() != null && !problem.getJavaStarterCode().isBlank()) return;

        String javaReturn = mapToJavaType(output);
        String pyReturn = mapToPythonType(output);

        // Parse input params
        String[] inputParts = input.split("[,+]\\s*");
        StringBuilder javaParams = new StringBuilder();
        StringBuilder pyParams = new StringBuilder();
        String[] paramNames = {"nums", "s", "target", "head", "root", "matrix", "arr", "val", "k", "n"};

        for (int i = 0; i < inputParts.length; i++) {
            String paramName = i < paramNames.length ? paramNames[i] : "arg" + i;
            // Use contextual naming based on type
            String part = inputParts[i].toLowerCase().trim();
            switch (part) {
                case "int_array": paramName = "nums"; break;
                case "string": paramName = (i == 0) ? "s" : "t"; break;
                case "int": 
                    if (i == 0) paramName = "n";
                    else if (method.toLowerCase().contains("k") || inputParts.length > 1 && i == 1) paramName = "k";
                    else paramName = "target";
                    break;
                case "binary_tree": paramName = (i == 0) ? "root" : "root" + (i + 1); break;
                case "linked_list": paramName = (i == 0) ? "head" : "head" + (i + 1); break;
                case "matrix": paramName = "matrix"; break;
                case "string_array": paramName = "words"; break;
            }

            if (i > 0) { javaParams.append(", "); pyParams.append(", "); }
            javaParams.append(mapToJavaType(part)).append(" ").append(paramName);
            pyParams.append(paramName).append(": ").append(mapToPythonType(part));
        }

        problem.setJavaStarterCode(
                "class Solution {\n    public " + javaReturn + " " + method + "(" + javaParams + ") {\n        \n    }\n}");
        problem.setPythonStarterCode(
                "class Solution:\n    def " + method + "(self, " + pyParams + ") -> " + pyReturn + ":\n        pass");
        problem.setJavascriptStarterCode(
                "class Solution {\n    " + method + "(" + String.join(", ",
                java.util.Arrays.stream(inputParts).map(p -> {
                    switch (p.trim()) {
                        case "int_array": return "nums";
                        case "string": return "s";
                        case "int": return "target";
                        case "binary_tree": return "root";
                        case "linked_list": return "head";
                        default: return "arg";
                    }
                }).toArray(String[]::new)) + ") {\n        \n    }\n}");
    }

    private String mapToJavaType(String format) {
        if (format == null) return "Object";
        switch (format.toLowerCase().trim()) {
            case "int": return "int";
            case "int_array": return "int[]";
            case "string": return "String";
            case "string_array": return "String[]";
            case "boolean": return "boolean";
            case "binary_tree": return "TreeNode";
            case "linked_list": return "ListNode";
            case "matrix": return "int[][]";
            default: return "Object";
        }
    }

    private String mapToPythonType(String format) {
        if (format == null) return "Any";
        switch (format.toLowerCase().trim()) {
            case "int": return "int";
            case "int_array": return "List[int]";
            case "string": return "str";
            case "string_array": return "List[str]";
            case "boolean": return "bool";
            case "binary_tree": return "Optional[TreeNode]";
            case "linked_list": return "Optional[ListNode]";
            case "matrix": return "List[List[int]]";
            default: return "Any";
        }
    }

    /**
     * Attempts to repair truncated JSON by closing any unclosed strings, arrays, and objects.
     */
    private String repairTruncatedJson(String json) {
        if (json == null || json.isEmpty()) return json;

        // Check if it already parses cleanly
        try {
            objectMapper.readTree(json);
            return json; // valid JSON
        } catch (Exception ignored) {
            // needs repair
        }

        StringBuilder sb = new StringBuilder(json);

        // Track state
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

        // If we're still inside a string, close it
        if (inString) {
            sb.append('"');
        }

        // Close any unclosed brackets/braces
        while (!stack.isEmpty()) {
            sb.append(stack.pop());
        }

        String repaired = sb.toString();
        log.debug("JSON repair applied: added {} closing characters", repaired.length() - json.length());
        return repaired;
    }
}
