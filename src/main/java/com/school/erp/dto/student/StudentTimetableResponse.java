package com.school.erp.dto.student;

import com.school.erp.dto.academic.TimetableEntryResponse;
import com.school.erp.dto.academic.TimetablePeriodResponse;

import java.time.LocalDate;
import java.util.List;

public record StudentTimetableResponse(
        Long studentId,
        String studentName,
        String admissionNo,
        String rollNumber,
        Long schoolId,
        String schoolName,
        Long classId,
        String className,
        Long sectionId,
        String sectionName,
        Long academicYearId,
        String academicYearName,
        String todayDayOfWeek,
        LocalDate todayDate,
        List<TimetablePeriodResponse> periods,
        List<TimetableEntryResponse> entries
) {}
