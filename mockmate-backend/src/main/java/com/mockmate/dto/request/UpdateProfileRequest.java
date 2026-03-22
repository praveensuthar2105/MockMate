package com.mockmate.dto.request;

import com.mockmate.model.ExperienceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileRequest(
        @NotBlank(message = "Name is required") String name,

        @NotNull(message = "Experience level is required") ExperienceLevel experienceLevel) {
}
