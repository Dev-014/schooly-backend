package com.school.erp.dto.finance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CollectionPlanRequest {
    @NotBlank(message = "Plan name is required")
    private String name;
    
    private String description;
    
    private Boolean isActive = true;

    @NotNull(message = "Items cannot be null")
    private List<CollectionPlanItemRequest> items;
}
