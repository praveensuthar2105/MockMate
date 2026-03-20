package com.mockmate.dto.response;

import java.time.LocalDateTime;

public record ResumeResponse(
        Long id,
        String fileName,
        String parsedJson,
        String skills,
        String summary,
        LocalDateTime uploadedAt) {
}
