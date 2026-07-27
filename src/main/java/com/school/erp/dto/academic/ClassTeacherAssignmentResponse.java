package com.school.erp.dto.academic;

public record ClassTeacherAssignmentResponse(
        Long id,
        Long schoolId,
        Long staffId,
        String staffName,
        String staffEmail,
        Long classId,
        String className,
        Long sectionId,
        String sectionName,
        Long academicYearId,
        String sessionName,
        String status
) {}
