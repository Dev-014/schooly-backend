package com.school.erp.dto.feecategory;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeeCategoryResponse {
    private Long id;
    private Long schoolId;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
