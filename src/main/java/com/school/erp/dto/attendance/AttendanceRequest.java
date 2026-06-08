package com.school.erp.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AttendanceRequest(
        @NotNull(message = "studentId is required")
        Long studentId,
        Long schoolId,
        @NotNull(message = "attendanceDate is required")
        LocalDate attendanceDate,
        @NotBlank(message = "status is required")
        String status
) {
}
