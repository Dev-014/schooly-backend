package com.school.erp.dto.academic;

public record SubjectResponse(
        Long id,
        Long schoolId,
        String code,
        String name,
        String type,
        Integer credits,
        String gradeLevel,
        String status
) {}
