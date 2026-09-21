package com.school.erp.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTicketStatusRequest {
    @NotBlank
    private String status;
    
    private String remark;
}
