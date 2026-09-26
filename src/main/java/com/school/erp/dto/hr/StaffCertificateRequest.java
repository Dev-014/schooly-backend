package com.school.erp.dto.hr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffCertificateRequest {
    @NotNull(message = "Staff ID is required")
    private Long staffId;

    @NotBlank(message = "Certificate type is required")
    private String certificateType;

    private LocalDate issueDate;
    private String remarks;
    private String status;
}
