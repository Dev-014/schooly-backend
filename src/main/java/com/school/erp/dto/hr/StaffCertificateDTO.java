package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffCertificateDTO {
    private Long id;
    private Long schoolId;
    private Long staffId;
    private String staffName;
    private String staffCode;
    private String department;
    private String designation;
    private String certificateType;
    private LocalDate issueDate;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}
