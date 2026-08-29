package com.school.erp.dto.finance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FeeInstallmentRequest {
    @NotNull(message = "Academic year is required")
    private Long academicYearId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private List<FeeInstallmentItemRequest> items;
}
