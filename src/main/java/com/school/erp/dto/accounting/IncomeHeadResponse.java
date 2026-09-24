package com.school.erp.dto.accounting;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IncomeHeadResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
