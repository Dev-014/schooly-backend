package com.school.erp.dto.parent;

public record ParentChildResponse(
        Long studentId,
        String name,
        String studentName,
        String admissionNo,
        Long schoolId,
        Long classId,
        String className,
        Long sectionId,
        String sectionName,
        String rollNumber
) {
    public ParentChildResponse(
            Long studentId,
            String name,
            String admissionNo,
            Long schoolId,
            Long classId
    ) {
        this(studentId, name, name, admissionNo, schoolId, classId, null, null, null, null);
    }
}
