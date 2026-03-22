package com.mockmate.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class CodeEvaluation {
    private String timeComplexity;
    private String spaceComplexity;
    private Integer correctness;
    private Integer codeQuality;
    private Integer naming;
    private Integer edgeCases;
    private Integer overallScore;
    private String feedback;
    private List<String> improvements;
    private Integer hintsUsed;
    private Integer testsPassed;
    private Integer testsTotal;
}
