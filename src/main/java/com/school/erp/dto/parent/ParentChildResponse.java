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
        String rollNumber,
        String parentName,
        String relation,
        String fatherName,
        String motherName,
        String guardianName
) {
    public ParentChildResponse(
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
        this(studentId, name, studentName, admissionNo, schoolId, classId, className, sectionId, sectionName, rollNumber, null, null, null, null, null);
    }

    public ParentChildResponse(
            Long studentId,
            String name,
            String admissionNo,
            Long schoolId,
            Long classId
    ) {
        this(studentId, name, name, admissionNo, schoolId, classId, null, null, null, null, null, null, null, null, null);
    }
}
