package com.school.erp.dto.hr;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class StaffAttendanceRequest {
    @NotNull(message = "Staff ID is required")
    private Long staffId;
    
    @NotNull(message = "Date is required")
    private LocalDate attendanceDate;
    
    @NotNull(message = "Status is required")
    private String status;
    
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String notes;
}
