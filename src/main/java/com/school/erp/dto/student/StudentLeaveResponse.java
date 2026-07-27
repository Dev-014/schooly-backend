package com.school.erp.dto.student;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentLeaveResponse(
        Long id,
        Long schoolId,
        Long studentId,
        String studentName,
        String admissionNo,
        String classKey,
        LocalDate applyDate,
        LocalDate fromDate,
        LocalDate toDate,
        Integer days,
        String reason,
        String status,
        String reply,
        LocalDateTime createdAt
) {}
