package com.school.erp.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceTokenUnregisterRequest(
        @NotBlank(message = "Device token is required")
        @Size(max = 512, message = "Device token must not exceed 512 characters")
        String token
) {
}
