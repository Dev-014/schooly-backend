package com.school.erp.dto.auth;

import java.util.List;

public record AuthUserResponse(
        Long id,
        String phone,
        String name,
        String email,
        String status,
        boolean newlyCreated,
        List<UserSchoolResponse> schools
) {
}
