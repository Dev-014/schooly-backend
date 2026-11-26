package com.school.erp.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CoCurricularGradeRequest {

    @NotNull(message = "Term ID is required")
    private Long termId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    private Long classId;

    private Long sectionId;

    private Long subjectId;

    private String grade;

    private Map<Long, String> subjectGrades;

    private String physicalEducationGrade;

    private String visualArtsGrade;

    private String performingArtsGrade;

    private String healthWellnessGrade;

    private String status = "COMPLETED";
}
