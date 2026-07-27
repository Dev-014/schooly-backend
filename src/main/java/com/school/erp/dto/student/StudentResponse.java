package com.school.erp.dto.student;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        Long userId,
        String name,
        String admissionNo,
        String rollNumber,
        String status,
        LocalDate admissionDate,
        Long schoolId,
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
