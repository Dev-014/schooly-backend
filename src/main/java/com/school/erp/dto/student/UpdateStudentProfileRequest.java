package com.school.erp.dto.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateStudentProfileRequest(
        @Size(max = 100, message = "firstName cannot exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "lastName cannot exceed 100 characters")
        String lastName,

        @Size(max = 50, message = "rollNumber cannot exceed 50 characters")
        String rollNumber,

        @Size(max = 30, message = "phone cannot exceed 30 characters")
        String phone,

        @Email(message = "Invalid email format")
        String email,

        String gender,
        LocalDate dateOfBirth,

        @Size(max = 10, message = "bloodGroup cannot exceed 10 characters")
        String bloodGroup,

        String religion,
        String caste,

        String address,
        String permanentAddress,
        String photoUrl,

        String guardianName,
        String guardianRelation,
        String guardianPhone,
        String guardianEmail,
        String guardianOccupation,

        String fatherName,
        String fatherPhone,
        String fatherEmail,
        String fatherOccupation,

        String motherName,
        String motherPhone,
        String motherEmail,
        String motherOccupation,

        String bankName,
        String bankAccountNo,
        String bankIfsc
) {
    public UpdateStudentProfileRequest(
            String firstName,
            String lastName,
            String phone,
            String email,
            String gender,
            LocalDate dateOfBirth,
            String bloodGroup,
            String religion,
            String caste,
            String address,
            String permanentAddress,
            String photoUrl,
            String guardianName,
            String guardianRelation,
            String guardianPhone,
            String guardianEmail,
            String guardianOccupation,
            String fatherName,
            String fatherPhone,
            String fatherEmail,
            String fatherOccupation,
            String motherName,
            String motherPhone,
            String motherEmail,
            String motherOccupation,
            String bankName,
            String bankAccountNo,
            String bankIfsc
    ) {
        this(firstName, lastName, null, phone, email, gender, dateOfBirth, bloodGroup, religion, caste,
                address, permanentAddress, photoUrl, guardianName, guardianRelation, guardianPhone, guardianEmail,
                guardianOccupation, fatherName, fatherPhone, fatherEmail, fatherOccupation, motherName, motherPhone,
                motherEmail, motherOccupation, bankName, bankAccountNo, bankIfsc);
    }
}
