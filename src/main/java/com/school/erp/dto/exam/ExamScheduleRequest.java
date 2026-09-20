package com.school.erp.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ExamScheduleRequest {

    @NotNull(message = "Exam setup ID is required")
    private Long examSetupId;

    private Long classId;

    private Long sectionId;

    private Long subjectId;

    private String subjectCode;

    private String subjectName;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private String roomNumber;

    private BigDecimal fullMarks = new BigDecimal("100.00");

    private BigDecimal passingMarks = new BigDecimal("35.00");

    private String instructions;

    private String componentType = "THEORY";

    private String status = "DRAFT";
}
