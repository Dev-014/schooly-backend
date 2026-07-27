package com.school.erp.dto.academic;

public record TimetableEntryResponse(
        Long id,
        Long schoolId,
        Long classId,
        String className,
        Long sectionId,
        String sectionName,
        Long academicYearId,
        String sessionName,
        String dayOfWeek,
        Long periodId,
        Integer periodNumber,
        String periodName,
        String startTime,
        String endTime,
        Boolean isBreak,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long teacherId,
        String teacherName,
        String roomNumber,
        String status
) {}
