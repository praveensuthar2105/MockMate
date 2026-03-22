package com.mockmate.dto.request;

import com.mockmate.model.InterviewType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewRequest {

    @NotBlank(message = "Company name is required")
    private String company;

    @NotBlank(message = "Difficulty level is required")
    private String difficulty;

    @NotBlank(message = "Job role is required")
    private String jobRole;

    private InterviewType type = InterviewType.FULL_MOCK;

    private Integer resumeDurationMins;
    private Integer dsaDurationMins;
    private Integer systemDesignDurationMins;
    private Integer hrDurationMins;
}
