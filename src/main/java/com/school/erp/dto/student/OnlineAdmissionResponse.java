package com.school.erp.dto.student;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OnlineAdmissionResponse(
        Long id,
        Long schoolId,
        String applicationId,
        String studentName,
        Long classId,
        String className,
        String fatherName,
        LocalDate dateOfBirth,
        String gender,
        Long categoryId,
        String categoryName,
        String mobileNumber,
        String email,
        String address,
        String previousSchool,
        String transactionStatus,
        String status,
        LocalDateTime appliedDate
) {
}
