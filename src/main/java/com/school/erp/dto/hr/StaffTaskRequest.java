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
public class StaffTaskRequest {
    @NotNull(message = "Staff ID is required")
    private Long staffId;

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String status;   // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}
