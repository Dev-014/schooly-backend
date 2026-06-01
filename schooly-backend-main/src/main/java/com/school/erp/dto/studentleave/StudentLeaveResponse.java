package com.school.erp.dto.studentleave;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentLeaveResponse(
        Long id,
        Long studentId,
        String studentName,
        Long schoolId,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        String status,
        Long approvedById,
        String approvedByName,
        LocalDateTime createdAt
) {
}
