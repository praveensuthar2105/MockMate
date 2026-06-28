package com.mockmate.dto.request;

import com.mockmate.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VoiceTranscriptRequest(
        @NotNull Role role,
        @NotBlank String content) {
}
