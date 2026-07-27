package com.school.erp.dto.student;

public record StudentStatsResponse(
        long totalStudents,
        long activeStudents,
        long inactiveStudents,
        int maxAllowedStudents,
        String planCode,
        String planName
) {}
