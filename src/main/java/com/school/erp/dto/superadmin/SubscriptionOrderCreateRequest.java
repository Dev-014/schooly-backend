package com.school.erp.dto.superadmin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscriptionOrderCreateRequest(
        @NotNull(message = "schoolId is required")
        Long schoolId,

        @NotBlank(message = "planName is required")
        String planName,

        @NotBlank(message = "billingCycle is required")
        String billingCycle, // MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY

        @NotNull(message = "studentCount is required")
        @Min(value = 1, message = "studentCount must be at least 1")
        Integer studentCount
) {}
