package com.school.erp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SwitchPersonaRequest {
    @NotBlank
    private String roleId;
}
