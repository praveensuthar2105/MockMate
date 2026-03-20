package com.mockmate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewRequest {

    @NotBlank(message = "Company name is required")
    private String company;

    @NotBlank(message = "Difficulty level is required")
    private String difficulty;
}
