package com.school.erp.dto.auth;

public record AuthUserResponse(
        Long id,
        String phone,
        String name,
        String email,
        String status,
        boolean newlyCreated
) {
}
