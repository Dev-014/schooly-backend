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
public class StudentReportCardResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNo;
    private String admissionNo;
    private String className;
    private String sectionName;
    private String termName;
    private String mode;
    private String templateName;
    private BigDecimal totalMarks;
    private BigDecimal maxMarks;
    private BigDecimal percentage;
    private String grade;
    private String division;
    private String status;
    private String fileUrl;
}
