package com.school.erp.dto.usermanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLogDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long schoolId;
    private String schoolName;
    private String role;
    private String module;
    private String action;
    private String ipAddress;
    private LocalDateTime timestamp;
}
