package com.school.erp.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record StudentReferralRequest(
        @NotNull(message = "School ID is required") Long schoolId,
        @NotBlank(message = "Referral by is required") String referralBy,
        @NotBlank(message = "Student name is required") String studentName,
        String email,
        @NotBlank(message = "Mobile is required") String mobile,
        String note,
        String status
) {}
