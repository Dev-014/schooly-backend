package com.school.erp.dto.exam;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ExamTermResponse {
    private Long id;
    private Long schoolId;
    private Long academicYearId;
    private String academicYearName;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate resultPublishDate;
    private String status;
    private Long examCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
