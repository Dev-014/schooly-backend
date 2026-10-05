package com.school.erp.dto.hr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolLeaveTypeRequest {
    @NotBlank(message = "Leave type name is required")
    private String name;

    @NotNull(message = "Days allowed is required")
    private Integer daysAllowed;

    private Boolean isPaid;

    private String applicableRoles;
}
