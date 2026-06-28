package com.mockmate.agent;

import com.mockmate.dto.response.PhaseScore;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@SystemMessage("You are an expert technical interviewer. Analyze the provided interview transcript and details, and return a structured evaluation. You must respond strictly with the requested JSON schema matching the fields in PhaseScore.")
public interface ScoringAgent {
    @UserMessage("Evaluate this interview phase:\n" +
            "Company: {{company}}\n" +
            "Difficulty: {{difficulty}}\n" +
            "Phase: {{phase}}\n\n" +
            "Transcript:\n" +
            "{{transcript}}")
    PhaseScore evaluate(
        @V("company") String company,
        @V("difficulty") String difficulty,
        @V("phase") String phase,
        @V("transcript") String transcript
    );
}
