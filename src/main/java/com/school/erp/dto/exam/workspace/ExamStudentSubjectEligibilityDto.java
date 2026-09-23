package com.school.erp.dto.exam.workspace;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamStudentSubjectEligibilityDto {
    private Long eligibilityId;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private String status;
}
