package com.mockmate.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class HintRequest {
    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Level is required")
    @Min(value = 1, message = "Hint level must be between 1 and 3")
    @Max(value = 3, message = "Hint level must be between 1 and 3")
    private Integer level;
}
