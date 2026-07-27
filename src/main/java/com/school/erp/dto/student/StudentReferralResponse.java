package com.school.erp.dto.student;

import java.time.LocalDateTime;

public record StudentReferralResponse(
        Long id,
        Long schoolId,
        String referralBy,
        String studentName,
        String email,
        String mobile,
        String note,
        String status,
        LocalDateTime createdAt
) {}
