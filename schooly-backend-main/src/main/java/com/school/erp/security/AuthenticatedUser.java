package com.school.erp.security;

import com.school.erp.entity.UserRole;

public record AuthenticatedUser(
        Long userId,
        Long schoolId,
        UserRole role
) {
}
