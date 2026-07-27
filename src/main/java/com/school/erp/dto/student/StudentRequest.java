package com.school.erp.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StudentRequest(
        Long userId,
        String name,
        @NotBlank(message = "admissionNo is required")
        String admissionNo,
        String rollNumber,
        @NotBlank(message = "status is required")
        String status,
        @NotNull(message = "admissionDate is required")
        LocalDate admissionDate,
        Long schoolId,
        @NotNull(message = "classId is required")
        Long classId,
        Long sectionId,
        Long academicYearId,
        String firstName,
        String lastName,
        String gender,
        LocalDate dateOfBirth,
        String bloodGroup,
        String religion,
        String nationality,
        String previousSchool,
        String address,
        String photoUrl,
        String guardianName,
        String guardianRelation,
        String guardianPhone,
        String guardianEmail,
        String guardianOccupation,
        Long categoryId,
        Long houseId
) {
}
