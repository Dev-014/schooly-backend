package com.school.erp.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExamMarkEntryRequest {

    @NotNull(message = "Exam setup ID is required")
    private Long examSetupId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    private Long classId;

    private Long sectionId;

    private BigDecimal marksObtained;

    private BigDecimal maxMarks = new BigDecimal("100.00");

    private String attendanceStatus = "PRESENT"; // PRESENT, ABSENT, MEDICAL_LEAVE

    private String remarks;
}
