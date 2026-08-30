package com.school.erp.dto.feestructure;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FeeStructureResponse {
    private Long id;
    private Long schoolId;
    private Long classId;
    private String className;
    private Long academicYearId;
    private String name;
    private String description;
    private Boolean isActive;
    private Long collectionPlanId;
    private String collectionPlanName;
    private LocalDateTime createdAt;
    private List<FeeStructureItemResponse> items;
}
