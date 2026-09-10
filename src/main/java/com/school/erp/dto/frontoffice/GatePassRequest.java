package com.school.erp.dto.frontoffice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatePassRequest {

    private String passNumber;

    @NotBlank(message = "Person name is required")
    private String personName;

    @NotBlank(message = "Role is required (STUDENT, STAFF, VISITOR)")
    private String role;

    private Long studentId;

    private Long staffId;

    private String classOrDepartment;

    @NotBlank(message = "Reason for exit is required")
    private String reasonForExit;

    @NotNull(message = "Pass date is required")
    private LocalDate passDate;

    @NotNull(message = "Exit time is required")
    private LocalTime exitTime;

    private LocalTime expectedReturnTime;

    @NotBlank(message = "Approved by is required")
    private String approvedBy;

    private Long approvedByStaffId;

    private String status;
}
