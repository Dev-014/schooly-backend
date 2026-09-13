package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardCriteriaCountResponse {
    private long studentCount; // e.g. 42
    private boolean readyForGeneration; // true
    private String message; // "42 students identified in the selected criteria. Ready for generation."
}
