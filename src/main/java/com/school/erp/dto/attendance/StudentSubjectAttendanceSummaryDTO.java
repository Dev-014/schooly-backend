package com.school.erp.dto.attendance;

public record StudentSubjectAttendanceSummaryDTO(
        Long subjectId,
        String subjectName,
        String subjectCode,
        long totalClasses,
        long attendedClasses,
        int percentage
) {
}
