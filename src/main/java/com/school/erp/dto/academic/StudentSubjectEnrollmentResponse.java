package com.school.erp.dto.academic;

public record StudentSubjectEnrollmentResponse(
        Long id,
        Long schoolId,
        Long studentId,
        String studentName,
        String admissionNo,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long academicYearId,
        String sessionName,
        String enrollmentType,
        String status
) {}
