package com.school.erp.dto.studentleave;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record StudentLeaveRequest(
        @NotNull(message = "startDate is required")
        LocalDate startDate,
        
        @NotNull(message = "endDate is required")
        LocalDate endDate,
        
        String reason
) {
}
