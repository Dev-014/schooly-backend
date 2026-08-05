package com.school.erp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddPerformanceReviewRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    @NotBlank(message = "Review cycle is required")
    private String reviewCycle;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    private Long reviewerId;
    private String comments;
    private String goals;
}
