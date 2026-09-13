package com.school.erp.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateReportCardRequest {

    @NotNull(message = "Term ID is required")
    private Long termId;

    private Long examSetupId;

    @NotNull(message = "Class ID is required")
    private Long classId;

    private Long sectionId;

    @Builder.Default
    private String mode = "TERM_WISE"; // TERM_WISE, EXAM_WISE

    @Builder.Default
    private String templateName = "Classic CBSE Standard";
}
