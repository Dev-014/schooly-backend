package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoCurricularItemResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String admissionNo;
    private String rollNumber;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private Long termId;
    private String termName;
    private String physicalEducationGrade;
    private String visualArtsGrade;
    private String performingArtsGrade;
    private String healthWellnessGrade;
    private String status;
}
