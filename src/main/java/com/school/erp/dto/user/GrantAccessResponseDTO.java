package com.school.erp.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GrantAccessResponseDTO {
    private boolean success;
    private String strategyUsed; // "EMAIL", "SMS", "MANUAL"
    private String message;
    
    // Only populated if strategyUsed is MANUAL
    private String generatedUsername;
    private String generatedPassword;
}
