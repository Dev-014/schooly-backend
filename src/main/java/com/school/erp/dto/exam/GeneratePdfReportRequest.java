package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePdfReportRequest {
    private Long termId;
    private Long classId;
    private Long sectionId;
    private Long subjectId;
    private String reportType; // TERM_WISE, SUBJECT_WISE, FAIL_STUDENT
}
