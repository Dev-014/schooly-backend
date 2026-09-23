package com.school.erp.dto.exam;

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
public class ExamReportPreviewItemResponse {
    private Long studentId;
    private String rollNo;
    private String studentName;
    private String initials;
    private BigDecimal theoryMarks; // e.g. 74.00
    private BigDecimal practicalMarks; // e.g. 19.00
    private BigDecimal totalMarks; // e.g. 93.00
    private String grade; // e.g. "A+"
    private BigDecimal percentage;
    private String division;
    private List<SubjectMarkInfo> subjects;
    private String status; // e.g. "PASS", "FAIL"
}
