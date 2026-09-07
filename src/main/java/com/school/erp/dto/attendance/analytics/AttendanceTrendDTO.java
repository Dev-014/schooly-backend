package com.school.erp.dto.attendance.analytics;

import java.time.LocalDate;

public record AttendanceTrendDTO(
    LocalDate date,
    int presentPercent
) {}
