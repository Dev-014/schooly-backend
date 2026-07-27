package com.school.erp.dto.student;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record OnlineAdmissionRequest(
        Long schoolId,
        @NotBlank String studentName,
        Long classId,
        String fatherName,
        LocalDate dateOfBirth,
        String gender,
        Long categoryId,
        String mobileNumber,
        String email,
        String address,
        String previousSchool,
        String transactionStatus
) {
}
