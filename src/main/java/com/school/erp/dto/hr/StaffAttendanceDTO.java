package com.school.erp.dto.hr;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

@Data
public class StaffAttendanceDTO {
    private Long id;
    private Long schoolId;
    private Long staffId;
    private String staffName;
    private LocalDate attendanceDate;
    private String status;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private BigDecimal workingHours;
    private String notes;
}
