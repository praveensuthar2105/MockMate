package com.mockmate.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CodeSubmitRequest {
    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Language is required")
    private com.mockmate.model.ProgrammingLanguage language;

    @NotBlank(message = "Code is required")
    @jakarta.validation.constraints.Size(max = 10000, message = "Code must be at most 10000 characters")
    private String code;
}
