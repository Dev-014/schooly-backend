package com.school.erp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetSubmitRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
