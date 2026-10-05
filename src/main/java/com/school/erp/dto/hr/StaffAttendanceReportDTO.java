package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAttendanceReportDTO {
    private Long schoolId;
    private Integer month;
    private Integer year;
    private Long totalStaff;
    private List<StaffAttendanceReportItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffAttendanceReportItem {
        private Long staffId;
        private String staffCode;
        private String staffName;
        private String department;
        private String designation;
        private Long daysPresent;
        private Long daysAbsent;
        private Long daysLate;
        private Long daysHalfDay;
        private Long daysOnLeave;
        private BigDecimal totalWorkingHours;
        private Double attendancePercentage;
    }
}
