package com.school.erp.dto.staff;

public record StaffStatsResponse(
        Long total,
        Long present,
        Long onLeave,
        Long departments
) {
}
