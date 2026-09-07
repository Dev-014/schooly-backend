package com.school.erp.dto.attendance.analytics;

public record GradeAttendanceDTO(
    String className,
    int capacity,
    double avgAttendance,
    double lateFrequency,
    String performance
) {}
