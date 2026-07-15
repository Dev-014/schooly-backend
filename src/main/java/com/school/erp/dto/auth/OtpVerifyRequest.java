package com.school.erp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpVerifyRequest(
        @NotBlank(message = "phone is required")
        String phone,

        @NotBlank(message = "otp is required")
        @Size(min = 4, max = 4, message = "OTP must be 4 digits")
        String otp
) {
}
