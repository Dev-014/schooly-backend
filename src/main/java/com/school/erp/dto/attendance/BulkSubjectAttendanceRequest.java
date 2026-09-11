package com.school.erp.dto.attendance;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record BulkSubjectAttendanceRequest(
        Long schoolId,
        @NotNull(message = "classId is required")
        Long classId,
        Long sectionId,
        @NotNull(message = "subjectId is required")
        Long subjectId,
        Long timetableEntryId,
        @NotNull(message = "attendanceDate is required")
        LocalDate attendanceDate,
        @NotEmpty(message = "entries cannot be empty")
        List<SubjectAttendanceEntry> entries
) {
    public record SubjectAttendanceEntry(
            @NotNull(message = "studentId is required")
            Long studentId,
            @NotNull(message = "status is required")
            String status,
            String remarks
    ) {}
}
