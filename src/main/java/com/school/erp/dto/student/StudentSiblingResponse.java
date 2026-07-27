package com.school.erp.dto.student;

public record StudentSiblingResponse(
        Long id,
        Long schoolId,
        Long primaryStudentId,
        Long siblingStudentId,
        String siblingName,
        String siblingAdmissionNo,
        String siblingClass,
        String siblingSection,
        String siblingAvatar,
        String relationship,
        String createdAt
) {
}
