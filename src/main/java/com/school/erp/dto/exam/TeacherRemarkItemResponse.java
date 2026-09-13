package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherRemarkItemResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String admissionNo;
    private String rollNumber;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private Long termId;
    private String termName;
    private String previousGrade;
    private String teacherRemarks;
    private String status;
}
