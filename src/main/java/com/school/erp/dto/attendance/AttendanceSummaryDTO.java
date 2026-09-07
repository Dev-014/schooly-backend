package com.school.erp.dto.attendance;

public record AttendanceSummaryDTO(
    int totalStudents,
    int present,
    int absent,
    int late,
    int presentPercent,
    int pendingLeaves
) {}
