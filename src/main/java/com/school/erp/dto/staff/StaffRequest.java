package com.school.erp.dto.staff;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StaffRequest(
        Long userId,
        Long schoolId,
        @NotNull(message = "departmentId is required")
        Long departmentId,
        String roleId,
        @NotNull(message = "designationId is required")
        Long designationId,
        @NotNull(message = "joiningDate is required")
        LocalDate joiningDate,
        @NotNull(message = "salary is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "salary must be greater than zero")
        BigDecimal salary,
        @NotBlank(message = "status is required")
        String status,
        String firstName,
        String lastName,
        String department,
        String designation,
        String photoUrl,
        String phone,
        String email
) {
}
