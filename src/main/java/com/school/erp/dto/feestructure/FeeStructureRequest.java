package com.school.erp.dto.feestructure;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FeeStructureRequest {
    private Long schoolId;
    
    @NotNull(message = "School class ID is required")
    private Long classId;

    @NotNull(message = "Academic year is required")
    private Long academicYearId;
    
    @NotBlank(message = "Structure name is required")
    private String name;
    
    private String description;
    
    private Boolean isActive;
    
    private Long collectionPlanId;
    
    @NotNull(message = "Items list cannot be null")
    @Valid
    private List<FeeStructureItemRequest> items;
}
