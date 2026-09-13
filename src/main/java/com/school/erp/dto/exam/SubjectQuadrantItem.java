package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectQuadrantItem {
    private String subjectName; // e.g. "MATHEMATICS", "PHYSICS", "BIOLOGY", "ENGLISH"
    private String grade; // e.g. "A+", "B+", "A-", "A+"
    private String trend; // e.g. "High", "Med", "Stable", "Peak"
}
