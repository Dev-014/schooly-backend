package com.school.erp.dto.importing;

import jakarta.validation.constraints.NotBlank;

public record ResolveErrorRequest(
        @NotBlank(message = "New value is required")
        String newValue
) {
}
