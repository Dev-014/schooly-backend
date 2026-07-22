package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminLogEntryDto {
    private Long id;
    private String date;
    private String time;
    private String actorName;
    private String actorRole;
    private String action;
    private String resourceType;
    private String schoolName;
    private String ipAddress;
    private String status; // "success" or "failed"
    private String message;
}
