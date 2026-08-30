package com.school.erp.dto.finance;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CollectionPlanResponse {
    private Long id;
    private Long schoolId;
    private String name;
    private String description;
    private Boolean isActive;
    private List<CollectionPlanItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
