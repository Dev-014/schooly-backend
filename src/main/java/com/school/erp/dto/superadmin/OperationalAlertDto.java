package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationalAlertDto {
    private String type; // e.g. "payment", "system", "security"
    private String message;
    private String severity; // e.g. "high", "medium", "low"
    private String timestamp;
}
