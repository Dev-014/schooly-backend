package com.school.erp.dto.feestructure;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FeeStructureRequest {
    private Long schoolId;
    
    private Long academicYearId;
    
    @NotBlank(message = "Structure name is required")
    private String name;
    
    private String description;
    
    private Boolean isActive;
    
    @NotNull(message = "Items list cannot be null")
    @Valid
    private List<FeeStructureItemRequest> items;
}
