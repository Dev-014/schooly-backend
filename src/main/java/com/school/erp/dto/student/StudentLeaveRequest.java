package com.school.erp.dto.student;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record StudentLeaveRequest(
        @NotNull(message = "School ID is required") Long schoolId,
        @NotNull(message = "Student ID is required") Long studentId,
        @NotNull(message = "From Date is required") LocalDate fromDate,
        @NotNull(message = "To Date is required") LocalDate toDate,
        Integer days,
        String reason,
        String status,
        String reply,
        UUID attachmentFileId,
        String attachmentFileName,
        String attachmentUrl
) {
    public StudentLeaveRequest(
            Long schoolId,
            Long studentId,
            LocalDate fromDate,
            LocalDate toDate,
            Integer days,
            String reason,
            String status,
            String reply
    ) {
        this(schoolId, studentId, fromDate, toDate, days, reason, status, reply, null, null, null);
    }
}
