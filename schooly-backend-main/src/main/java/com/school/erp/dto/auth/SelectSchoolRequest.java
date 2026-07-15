package com.school.erp.dto.auth;

import jakarta.validation.constraints.NotNull;

public record SelectSchoolRequest(
        @NotNull(message = "userId is required")
        Long userId,
        @NotNull(message = "schoolId is required")
        Long schoolId
) {
}
