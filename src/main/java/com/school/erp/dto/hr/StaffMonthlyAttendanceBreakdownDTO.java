package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffMonthlyAttendanceBreakdownDTO {
    private Long staffId;
    private String staffName;
    private String staffCode;
    private String department;
    private String designation;
    private Integer month;
    private Integer year;
    private Integer totalDays;
    private Long presentCount;
    private Long absentCount;
    private Long lateCount;
    private Long halfDayCount;
    private Long onLeaveCount;
    private Long unmarkedCount;
    private Double attendancePercentage;
    private List<StaffAttendanceDTO> records;
}
