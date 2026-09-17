package com.school.erp.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkExamAttendanceRequest {

    @NotEmpty(message = "Attendance records cannot be empty")
    @Valid
    private List<ExamAttendanceRecordRequest> records;
}
