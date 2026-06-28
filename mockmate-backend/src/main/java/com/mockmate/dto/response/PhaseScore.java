package com.mockmate.dto.response;

import java.util.List;

public record PhaseScore(
    int score,
    String feedback,
    List<String> strengths,
    List<String> weaknesses,
    List<String> recommendations
) {}
