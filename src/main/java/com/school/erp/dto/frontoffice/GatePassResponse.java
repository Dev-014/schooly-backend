package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatePassResponse {

    private Long id;
    private Long schoolId;
    private String passNumber;
    private String personName;
    private String role;
    private Long studentId;
    private Long staffId;
    private String classOrDepartment;
    private String reasonForExit;
    private LocalDate passDate;
    private LocalTime exitTime;
    private LocalTime expectedReturnTime;
    private String approvedBy;
    private Long approvedByStaffId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
