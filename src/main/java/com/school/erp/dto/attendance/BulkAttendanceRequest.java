package com.school.erp.dto.attendance;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record BulkAttendanceRequest(
        Long schoolId,
        @NotNull(message = "attendanceDate is required")
        LocalDate attendanceDate,
        @NotEmpty(message = "entries cannot be empty")
        List<AttendanceEntry> entries
) {
    public record AttendanceEntry(
            @NotNull(message = "studentId is required")
            Long studentId,
            @NotNull(message = "status is required")
            String status
    ) {}
}
