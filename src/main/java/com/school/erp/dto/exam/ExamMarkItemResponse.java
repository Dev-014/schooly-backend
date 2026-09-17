package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExamMarkItemResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String admissionNo;
    private String rollNumber;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private Long subjectId;
    private String subjectName;
    private Long examSetupId;
    private String examName;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;
    private String attendanceStatus;
    private String remarks;
    private LocalDateTime updatedAt;
}
