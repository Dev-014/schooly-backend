package com.school.erp.dto.parent;

public record ParentChildResponse(
        Long studentId,
        String name,
        String admissionNo,
        Long schoolId,
        Long classId
) {
}
