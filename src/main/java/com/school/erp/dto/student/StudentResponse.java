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
        String className,
        Long sectionId,
        String sectionName,
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
        Long houseId,
        Long familyId
) {
    public StudentResponse(
            Long id, Long userId, String name, String admissionNo, String rollNumber,
            String status, LocalDate admissionDate, Long schoolId, Long classId,
            String className, Long sectionId, Long academicYearId, String firstName,
            String lastName, String gender, LocalDate dateOfBirth, String bloodGroup,
            String religion, String nationality, String previousSchool, String address,
            String photoUrl, String guardianName, String guardianRelation,
            String guardianPhone, String guardianEmail, String guardianOccupation,
            Long categoryId, Long houseId, Long familyId
    ) {
        this(id, userId, name, admissionNo, rollNumber, status, admissionDate, schoolId,
                classId, className, sectionId, null, academicYearId, firstName, lastName,
                gender, dateOfBirth, bloodGroup, religion, nationality, previousSchool,
                address, photoUrl, guardianName, guardianRelation, guardianPhone,
                guardianEmail, guardianOccupation, categoryId, houseId, familyId);
    }
}
