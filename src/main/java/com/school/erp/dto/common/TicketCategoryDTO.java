package com.school.erp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketCategoryDTO {
    private Long id;
    private String name;
    private String department;
    private String status;
}
