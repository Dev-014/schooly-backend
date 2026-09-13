package com.school.erp.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherRemarkRequest {

    @NotNull(message = "Term ID is required")
    private Long termId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    private Long classId;

    private Long sectionId;

    private String previousGrade;

    private String teacherRemarks;

    private String status = "COMPLETED";
}
