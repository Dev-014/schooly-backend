package com.school.erp.dto.student;

public record StudentCategoryResponse(
        Long id,
        Long schoolId,
        String name,
        String description
) {
}
