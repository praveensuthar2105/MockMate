package com.mockmate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillDistribution {
    private String skill;
    private Integer score;
    private Integer fullMark;
}
