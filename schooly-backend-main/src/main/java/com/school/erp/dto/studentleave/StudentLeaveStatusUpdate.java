package com.school.erp.dto.studentleave;

import jakarta.validation.constraints.NotBlank;

public record StudentLeaveStatusUpdate(
        @NotBlank(message = "status is required")
        String status
) {
}
