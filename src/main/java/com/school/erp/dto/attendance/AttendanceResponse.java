package com.school.erp.dto.attendance;

import java.time.LocalDate;

public record AttendanceResponse(
        Long id,
        Long studentId,
        Long schoolId,
        LocalDate attendanceDate,
        String status
) {
}
