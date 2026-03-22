package com.mockmate.dto.code;

import lombok.Data;
import java.util.List;

@Data
public class DsaProblem {
    private Long id;
    private String title;
    private String description;
    private List<String> constraints;
    private List<ProblemExample> examples;
    private List<TestCase> testCases;
    private List<ProblemHint> hints;
    private String difficulty;
    private String timeComplexityExpected;
    private String spaceComplexityExpected;
}
