package com.school.erp.dto.academic;

public record SectionResponse(
        Long id,
        Long classId,
        String className,
        Long schoolId,
        String name,
        String roomNumber,
        Integer capacity
) {}
