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
    private Long examSetupId;
    private String reportType; // TERM_WISE, EXAM_WISE, FAIL_STUDENT
}
