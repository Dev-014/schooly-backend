package com.school.erp.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkExamScheduleRequest {

    @NotNull(message = "Exam setup ID is required")
    private Long examSetupId;

    @NotNull(message = "Class ID is required")
    private Long classId;

    private Long sectionId;

    @NotEmpty(message = "At least one schedule item is required")
    @Valid
    private List<ExamScheduleRequest> schedules;
}
