package com.mockmate.dto.response;

import com.mockmate.dto.code.DsaProblem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DsaStatusResponse {
    private DsaProblem problem;
    private boolean submitted;
    private CodeEvaluation evaluation;
}
