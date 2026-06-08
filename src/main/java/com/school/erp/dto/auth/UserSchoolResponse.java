package com.school.erp.dto.auth;

public record UserSchoolResponse(
        Long schoolId,
        String schoolName,
        String schoolCode,
        String role,
        String status
) {
}
