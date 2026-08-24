package com.school.erp.dto.feecategory;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeeCategoryRequest {
    private Long schoolId;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String description;
    
    private Boolean isActive;
}
