package com.school.erp.dto.student;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record StudentLeaveRequest(
        @NotNull(message = "School ID is required") Long schoolId,
        @NotNull(message = "Student ID is required") Long studentId,
        @NotNull(message = "From Date is required") LocalDate fromDate,
        @NotNull(message = "To Date is required") LocalDate toDate,
        Integer days,
        String reason,
        String status,
        String reply
) {}
