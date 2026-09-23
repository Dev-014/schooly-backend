package com.school.erp.dto.exam.workspace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamApplicabilityDto {
    private Long id;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
}
