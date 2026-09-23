package com.school.erp.dto.exam.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamStudentEligibilityDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String admissionNo;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private String status; // ELIGIBLE, EXCLUDED
    private String remarks;
    private List<ExamStudentSubjectEligibilityDto> subjects;
}
