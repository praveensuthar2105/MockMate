package com.mockmate.dto.response;

import java.time.Instant;

public record VoiceSessionTokenResponse(
        String token,
        String model,
        Instant expiresAt) {
}
