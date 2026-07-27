package com.school.erp.dto.academic;

public record TimetablePeriodResponse(
        Long id,
        Long schoolId,
        Integer periodNumber,
        String name,
        String startTime,
        String endTime,
        Boolean isBreak
) {}
