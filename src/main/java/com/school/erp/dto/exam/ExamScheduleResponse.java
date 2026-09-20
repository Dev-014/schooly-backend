package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class ExamScheduleResponse {
    private Long id;
    private Long schoolId;
    private Long examSetupId;
    private String examName;
    private String termName;
    private Long classId;
    private String className;
    private Long sectionId;
    private String sectionName;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private String componentType;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomNumber;
    private BigDecimal fullMarks;
    private BigDecimal passingMarks;
    private String instructions;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
