package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAttendanceStatsDTO {
    private LocalDate date;
    private long totalStaff;
    private long presentCount;
    private long absentCount;
    private long lateCount;
    private long halfDayCount;
    private long onLeaveCount;
    private long unmarkedCount;
    private double attendancePercentage;
}
