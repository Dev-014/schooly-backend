package com.school.erp.dto.attendance;

import java.time.LocalDate;

public record SubjectAttendanceResponse(
        Long id,
        Long schoolId,
        Long studentId,
        String studentName,
        Long classId,
        String className,
        Long sectionId,
        String sectionName,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long timetableEntryId,
        LocalDate attendanceDate,
        String status,
        String remarks
) {
}
