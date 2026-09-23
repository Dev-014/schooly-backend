package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectMarkInfo {
    private String subjectName;
    private BigDecimal obtainedMarks;
    private BigDecimal maxMarks;
    private String grade;
}
