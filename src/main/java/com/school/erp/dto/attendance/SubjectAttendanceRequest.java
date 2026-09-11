package com.school.erp.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SubjectAttendanceRequest(
        Long schoolId,
        @NotNull(message = "studentId is required")
        Long studentId,
        @NotNull(message = "classId is required")
        Long classId,
        Long sectionId,
        @NotNull(message = "subjectId is required")
        Long subjectId,
        Long timetableEntryId,
        @NotNull(message = "attendanceDate is required")
        LocalDate attendanceDate,
        @NotBlank(message = "status is required")
        String status,
        String remarks
) {
}
