package com.school.erp.dto.staff;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StaffResponse(
        Long id,
        Long userId,
        Long schoolId,
        Long departmentId,
        Long designationId,
        LocalDate joiningDate,
        BigDecimal salary,
        String status
) {
}
