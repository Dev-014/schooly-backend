package com.school.erp.dto.auth;

public record AuthTokenResponse(
        Long userId,
        Long schoolId,
        String role,
        String accessToken,
        String refreshToken,
        java.util.List<PermissionContextDto> permissions,
        java.util.List<PersonaDto> availablePersonas,
        PersonaDto activePersona
) {
}
