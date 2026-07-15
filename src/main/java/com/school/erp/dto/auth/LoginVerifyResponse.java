package com.school.erp.dto.auth;

import java.util.List;

public record LoginVerifyResponse(
        Long userId,
        String phone,
        String name,
        String email,
        String primaryRole,
        List<String> roles,
        List<UserSchoolResponse> schools,
        boolean requiresSchoolSelection,
        boolean requiresStudentSelection,
        List<StudentSummaryDto> students,
        String accessToken,
        String refreshToken,
        List<String> permissions
) {
}
