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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DsaProblemService {

    private final ChatLanguageModel chatLanguageModel;
    private final InterviewSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    public DsaProblem generateProblem(InterviewSession session) {
        if (session.getReportJson() != null && !session.getReportJson().isEmpty()) {
            try {
                return objectMapper.readValue(session.getReportJson(), DsaProblem.class);
            } catch (Exception e) {
                log.warn("Failed to parse existing DsaProblem from reportJson", e);
            }
        }

        String company = session.getCompany();
        String difficulty = session.getDifficulty() != null ? session.getDifficulty().name() : "EASY";

        String prompt = "Generate a DSA coding problem for a " + difficulty + " level interview\n" +
                "at " + company + ". Return ONLY valid JSON in this exact format:\n" +
                "{\n" +
                "  \"title\": \"string\",\n" +
                "  \"description\": \"string\",\n" +
                "  \"constraints\": [\"string\"],\n" +
                "  \"examples\": [{ \"input\": \"string\", \"output\": \"string\", \"explanation\": \"string\" }],\n" +
                "  \"testCases\": [{ \"input\": \"string\", \"expectedOutput\": \"string\" }],\n" +
                "  \"hints\": [\n" +
                "    { \"level\": 1, \"hint\": \"...\" },\n" +
                "    { \"level\": 2, \"hint\": \"...\" },\n" +
                "    { \"level\": 3, \"hint\": \"...\" }\n" +
                "  ],\n" +
                "  \"difficulty\": \"string\",\n" +
                "  \"timeComplexityExpected\": \"string\",\n" +
                "  \"spaceComplexityExpected\": \"string\",\n" +
                "  \"javaStarterCode\": \"class Solution { ... }\",\n" +
                "  \"javaTestRunner\": \"public class Main { ... reads stdin, calls Solution, prints output ... }\",\n"
                +
                "  \"pythonStarterCode\": \"class Solution: ...\",\n" +
                "  \"pythonTestRunner\": \"if __name__ == '__main__': ... reads sys.stdin, calls Solution ...\",\n" +
                "  \"javascriptStarterCode\": \"class Solution { ... }\",\n" +
                "  \"javascriptTestRunner\": \"const Solution = require('./solution'); ... reads stdin ...\"\n" +
                "}\n" +
                "IMPORTANT: The TestRunner must be a complete Main class that reads from stdin, calls the Solution class/method, and prints the result to stdout. For Java, Main.java will have Solution.java available.\n"
                +
                "Generate at least 5 test cases.";

        DsaProblem problem = null;

        List<InterviewSession> recentSessions = sessionRepository
                .findByUserIdOrderByStartedAtDesc(session.getUser().getId());

        for (int attempts = 0; attempts < 2; attempts++) {
            try {
                var request = ChatRequest.builder()
                        .messages(List.of(
                                SystemMessage.from(
                                        "You are an expert technical interviewer producing clean, parseable JSON."),
                                UserMessage.from(prompt)))
                        .build();

                String response = chatLanguageModel.chat(request).aiMessage().text();
                response = response.replaceAll("```json", "").replaceAll("```", "").trim();

                problem = objectMapper.readValue(response, DsaProblem.class);
                boolean duplicateFound = false;
                for (int i = 0; i < Math.min(5, recentSessions.size()); i++) {
                    InterviewSession pastSession = recentSessions.get(i);
                    if (!pastSession.getId().equals(session.getId()) && pastSession.getReportJson() != null) {
                        try {
                            DsaProblem pastProblem = objectMapper.readValue(pastSession.getReportJson(),
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

        persistProblem(session, problem);

        return problem;
    }

    @Transactional
    public void persistProblem(InterviewSession session, DsaProblem problem) {
        try {
            session.setReportJson(objectMapper.writeValueAsString(problem));
            sessionRepository.save(session);
        } catch (Exception e) {
            log.error("Failed to save DsaProblem to session", e);
        }
    }

    private DsaProblem getFallbackProblem(String difficulty) {
        DsaProblem problem = new DsaProblem();
        problem.setConstraints(List.of("Constraint 1", "Constraint 2"));
        problem.setExamples(new ArrayList<>());

        List<ProblemHint> hints = new ArrayList<>();
        ProblemHint h1 = new ProblemHint();
        h1.setLevel(1);
        h1.setHint("Hint 1");
        ProblemHint h2 = new ProblemHint();
        h2.setLevel(2);
        h2.setHint("Hint 2");
        ProblemHint h3 = new ProblemHint();
        h3.setLevel(3);
        h3.setHint("Hint 3");
        hints.add(h1);
        hints.add(h2);
        hints.add(h3);
        problem.setHints(hints);
        problem.setDifficulty(difficulty);

        if ("HARD".equalsIgnoreCase(difficulty)) {
            problem.setTitle("Word Break II");
            problem.setDescription(
                    "Given a string s and a dictionary of strings wordDict, add spaces in s to construct a sentence where each word is a valid dictionary word.");
            problem.setTimeComplexityExpected("O(2^N)");
            problem.setSpaceComplexityExpected("O(2^N)");
            problem.setTestCases(List.of(
                    createTestCase("catsanddog\ncats,dog,sand,and,cat", "cats and dog\ncat sand dog"),
                    createTestCase("pineapplepenapple\napple,pen,applepen,pine,pineapple",
                            "pine apple pen apple\npineapple pen apple\npine applepen apple"),
                    createTestCase("catsandog\ncats,dog,sand,and,cat", "")));
            problem.setExamples(toExamples(problem.getTestCases()));

            problem.setJavaStarterCode(
                    "class Solution {\n    public List<String> wordBreak(String s, List<String> wordDict) {\n        \n    }\n}");
            problem.setJavaTestRunner(
                    "import java.util.*;\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        String s = sc.nextLine();\n        List<String> dict = Arrays.asList(sc.nextLine().split(\",\"));\n        List<String> res = new Solution().wordBreak(s, dict);\n        Collections.sort(res);\n        for(String str : res) System.out.println(str);\n    }\n}");
            problem.setPythonStarterCode(
                    "class Solution:\n    def wordBreak(self, s: str, wordDict: List[str]) -> List[str]:\n        pass");
        } else if ("MEDIUM".equalsIgnoreCase(difficulty)) {
            problem.setTitle("Longest Substring Without Repeating Characters");
            problem.setDescription(
                    "Given a string s, find the length of the longest substring without repeating characters.");
            problem.setTimeComplexityExpected("O(N)");
            problem.setSpaceComplexityExpected("O(min(N, M))");
            problem.setTestCases(List.of(
                    createTestCase("abcabcbb", "3"),
                    createTestCase("bbbbb", "1"),
                    createTestCase("pwwkew", "3")));
            problem.setExamples(toExamples(problem.getTestCases()));

            problem.setJavaStarterCode(
                    "class Solution {\n    public int lengthOfLongestSubstring(String s) {\n        \n    }\n}");
            problem.setJavaTestRunner(
                    "import java.util.*;\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        String s = sc.hasNextLine() ? sc.nextLine() : \"\";\n        System.out.println(new Solution().lengthOfLongestSubstring(s));\n    }\n}");
            problem.setPythonStarterCode(
                    "class Solution:\n    def lengthOfLongestSubstring(self, s: str) -> int:\n        pass");
        } else {
            problem.setTitle("Two Sum");
            problem.setDescription(
                    "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.");
            problem.setTimeComplexityExpected("O(N)");
            problem.setSpaceComplexityExpected("O(N)");
            problem.setTestCases(List.of(
                    createTestCase("2 7 11 15\n9", "[0, 1]"),
                    createTestCase("3 2 4\n6", "[1, 2]"),
                    createTestCase("3 3\n6", "[0, 1]")));
            problem.setExamples(toExamples(problem.getTestCases()));

            // Templates for Two Sum
            problem.setJavaStarterCode(
                    "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        \n    }\n}");
            problem.setJavaTestRunner(
                    "import java.util.*;\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int[] nums = Arrays.stream(sc.nextLine().split(\" \")).mapToInt(Integer::parseInt).toArray();\n        int target = Integer.parseInt(sc.nextLine());\n        int[] res = new Solution().twoSum(nums, target);\n        System.out.println(Arrays.toString(res));\n    }\n}");
            problem.setPythonStarterCode(
                    "class Solution:\n    def twoSum(self, nums: List[int], target: int) -> List[int]:\n        pass");
            problem.setPythonTestRunner(
                    "import sys\nif __name__ == '__main__':\n    nums = list(map(int, sys.stdin.readline().split()))\n    target = int(sys.stdin.readline())\n    print(Solution().twoSum(nums, target))");
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
}
