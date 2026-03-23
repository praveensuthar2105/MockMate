package com.mockmate.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiParsingService {

    private final ChatLanguageModel chatModel;

    public GeminiParsingService(@Value("${langchain4j.googleai.gemini.api-key:}") String apiKey) {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.chatModel = GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName("gemini-1.5-flash")
                    .temperature(0.1) // Low temperature for factual extraction
                    .build();
        } else {
            this.chatModel = null;
        }
    }

    public String parseResume(String rawText) {
        if (chatModel == null) {
            return generateFallbackJson();
        }

        String prompt = "Extract the following information from the resume text into a valid JSON object. " +
                "The JSON must strictly match this structure: \n" +
                "{\n" +
                "  \"name\": \"Full Name\",\n" +
                "  \"email\": \"Email Address\",\n" +
                "  \"skills\": [\"Skill 1\", \"Skill 2\"],\n" +
                "  \"projects\": [\n" +
                "    {\n" +
                "      \"name\": \"Project Name\",\n" +
                "      \"tech\": [\"Tech 1\", \"Tech 2\"],\n" +
                "      \"desc\": \"Short Description\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"education\": \"Degree, University, Year\",\n" +
                "  \"summary\": \"Brief professional summary\"\n" +
                "}\n\n" +
                "Do not include any markdown formatting, code blocks like ```json, or explanations. Only output the raw JSON string.\n\n" +
                "Resume Text:\n" + rawText;

        try {
            String response = chatModel.generate(prompt);
            return cleanJsonResponse(response);
        } catch (Exception e) {
            // Log error here in a real app
            return generateFallbackJson();
        }
    }

    private String cleanJsonResponse(String response) {
        String clean = response.trim();
        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        }
        if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        return clean.trim();
    }

    private String generateFallbackJson() {
        return "{\n" +
                "  \"name\": \"Unknown Name\",\n" +
                "  \"email\": \"unknown@example.com\",\n" +
                "  \"skills\": [],\n" +
                "  \"projects\": [],\n" +
                "  \"education\": \"Not specified\",\n" +
                "  \"summary\": \"Resume could not be parsed automatically.\"\n" +
                "}";
    }
}
