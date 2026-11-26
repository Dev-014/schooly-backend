package com.school.erp.dto.exam.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubjectConfigDto {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private String subjectType;
    private List<String> assignedClasses;
    
    private BigDecimal maxMarks;
    private BigDecimal passingMarks;
    private BigDecimal theoryMarks;
    private BigDecimal practicalMarks;
    private BigDecimal internalMarks;
}
