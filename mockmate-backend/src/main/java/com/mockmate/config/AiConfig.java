package com.mockmate.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${app.gemini.api-key}")
    private String geminiApiKey;

    @Value("${app.gemini.model}")
    private String modelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(modelName)
                .temperature(0.7)
                .maxOutputTokens(2048)
                .maxRetries(3)
                .build();
    }
}
