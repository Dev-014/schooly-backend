package com.school.erp.dto.feestructure;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FeeStructureResponse {
    private Long id;
    private Long schoolId;
    private Long academicYearId;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<FeeStructureItemResponse> items;
}
