package com.school.erp.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GrantAccessDTO {
    @NotNull
    private Long entityId;
    
    @NotNull
    private String entityType; // "STUDENT", "STAFF", "PARENT"
}
