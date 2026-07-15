package com.school.erp.dto.auth;

public record StudentSummaryDto(
        Long id,
        String name,
        String admissionNo,
        String className,
        Long schoolId
) {
}
