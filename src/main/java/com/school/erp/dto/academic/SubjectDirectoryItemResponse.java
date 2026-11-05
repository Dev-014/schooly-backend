package com.school.erp.dto.academic;

public record SubjectDirectoryItemResponse(
        Long subjectId,
        String subjectName,
        String subjectCode,
        String subjectType,
        Integer credits,
        String gradeLevel,
        Long classId,
        String className,
        Long sectionId,
        String sectionName,
        String academicYearName,
        Long teacherId,
        String teacherName,
        String teacherEmail,
        String teacherDesignation,
        String teacherDepartment,
        String status
) {}

