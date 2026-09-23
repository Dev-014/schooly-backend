package com.school.erp.dto.exam.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubjectConfigDto {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    
    private BigDecimal maxMarks;
    private BigDecimal passingMarks;
    private BigDecimal theoryMarks;
    private BigDecimal practicalMarks;
    private BigDecimal internalMarks;
}
