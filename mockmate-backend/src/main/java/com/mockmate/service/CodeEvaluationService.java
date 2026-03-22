package com.mockmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockmate.dto.code.DsaProblem;
import com.mockmate.dto.code.ExecutionResult;
import com.mockmate.dto.response.CodeEvaluation;
import com.mockmate.model.CodeSubmission;
import com.mockmate.repository.CodeSubmissionRepository;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeEvaluationService {

    private final ChatLanguageModel chatLanguageModel;
    private final CodeSubmissionRepository codeSubmissionRepository;
    private final ObjectMapper objectMapper;

    public CodeEvaluation evaluate(String code, String language, DsaProblem problem, ExecutionResult executionResult,
            CodeSubmission submission) {
        String prompt = "You are a senior engineer reviewing a candidate's solution.\n" +
                "Problem: " + problem.getTitle() + "\n" +
                "Expected time complexity: " + problem.getTimeComplexityExpected() + "\n" +
                "Expected space complexity: " + problem.getSpaceComplexityExpected() + "\n" +
                "Language: " + language + "\n" +
                "Test results: " + executionResult.getPassedCount() + "/" + executionResult.getTotalCount()
                + " passed\n" +
                "Code submitted:\n" + code + "\n\n" +
                "Evaluate and return ONLY valid JSON:\n" +
                "{\n" +
                "  \"timeComplexity\": \"string (Big-O notation)\",\n" +
                "  \"spaceComplexity\": \"string (Big-O notation)\",\n" +
                "  \"correctness\": number (0-100),\n" +
                "  \"codeQuality\": number (0-100),\n" +
                "  \"naming\": number (0-100),\n" +
                "  \"edgeCases\": number (0-100),\n" +
                "  \"overallScore\": number (0-100),\n" +
                "  \"feedback\": \"string (2-3 sentences of specific feedback)\",\n" +
                "  \"improvements\": [\"string (2-3 specific improvement suggestions)\"]\n" +
                "}";

        CodeEvaluation evaluation = null;

        try {
            var request = ChatRequest.builder()
                    .messages(List.of(
                            SystemMessage
                                    .from("You are an expert technical interviewer producing clean, parseable JSON."),
                            UserMessage.from(prompt)))
                    .build();

            // Explicit timeout using CompletableFuture
            java.util.concurrent.CompletableFuture<String> future = java.util.concurrent.CompletableFuture
                    .supplyAsync(() -> chatLanguageModel.chat(request).aiMessage().text());
            String response = future.get(45, java.util.concurrent.TimeUnit.SECONDS);

            String jsonOnly = extractJson(response);
            if (jsonOnly == null || jsonOnly.isEmpty()) {
                throw new RuntimeException("No valid JSON found in Gemini response: " + response);
            }

            evaluation = objectMapper.readValue(jsonOnly, CodeEvaluation.class);
        } catch (Exception e) {
            log.error("Failed to evaluate code with Gemini. Raw response was logged if available.", e);
            evaluation = new CodeEvaluation();
            evaluation.setTimeComplexity("Unknown");
            evaluation.setSpaceComplexity("Unknown");
            evaluation.setCorrectness(executionResult.isAllPassed() ? 100
                    : (executionResult.getPassedCount() * 100 / Math.max(1, executionResult.getTotalCount())));
            evaluation.setCodeQuality(70);
            evaluation.setNaming(70);
            evaluation.setEdgeCases(70);
            evaluation.setOverallScore(evaluation.getCorrectness());
            evaluation.setFeedback("Fallback evaluation used due to AI service disruption.");
            evaluation.setImprovements(List.of("Review Big-O analysis manually."));
        }

        return persistEvaluation(code, language, executionResult, submission, evaluation);
    }

    private CodeEvaluation persistEvaluation(String code, String language, ExecutionResult executionResult,
            CodeSubmission submission, CodeEvaluation evaluation) {
        int clampedScore = evaluation.getOverallScore() == null ? 0 : evaluation.getOverallScore();
        if (clampedScore > 100)
            clampedScore = 100;
        if (clampedScore < 0)
            clampedScore = 0;

        int mask = submission.getHintsUsed() != null ? submission.getHintsUsed() : 0;
        int uniqueHintsUsed = Integer.bitCount(mask);
        int finalScore = clampedScore - (uniqueHintsUsed * 10);
        if (finalScore < 0)
            finalScore = 0;

        evaluation.setOverallScore(finalScore);
        evaluation.setHintsUsed(uniqueHintsUsed);
        evaluation.setTestsPassed(executionResult.getPassedCount());
        evaluation.setTestsTotal(executionResult.getTotalCount());

        submission.setCode(code);
        submission.setLanguage(language);
        submission.setTimeComplexity(evaluation.getTimeComplexity());
        submission.setSpaceComplexity(evaluation.getSpaceComplexity());
        submission.setScore(finalScore);
        submission.setSubmitted(true);

        try {
            submission.setTestResultsJson(objectMapper.writeValueAsString(executionResult));
            submission.setEvaluationJson(objectMapper.writeValueAsString(evaluation));
        } catch (Exception e) {
            log.error("Failed to serialize execution results or evaluation", e);
            if (submission.getTestResultsJson() == null) {
                submission
                        .setTestResultsJson("{\"error\": \"Exception during serialization: " + e.getMessage() + "\"}");
            }
        }

        codeSubmissionRepository.save(submission);

        return evaluation;
    }

    private String extractJson(String text) {
        if (text == null)
            return null;

        // Find the first '{' and the last '}'
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');

        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1);
        }

        // Fallback to markdown cleaning if braces not found (unlikely for Gemini JSON
        // output)
        return text.replaceAll("```json", "").replaceAll("```", "").trim();
    }
}
