package com.school.erp.dto.student;

public record StudentHouseResponse(
        Long id,
        Long schoolId,
        String name,
        String colorCode,
        String description,
        Long studentCount
) {
}
