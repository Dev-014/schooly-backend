package com.school.erp.dto.academic;

public record ClassSubjectAssignmentResponse(
        Long id,
        Long schoolId,
        Long classId,
        String className,
        Long sectionId,
        String sectionName,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long academicYearId,
        String sessionName,
        String subjectType,
        String status
) {}
