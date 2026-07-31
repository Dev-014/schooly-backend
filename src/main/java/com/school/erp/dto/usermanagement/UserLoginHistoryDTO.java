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
public class UserLoginHistoryDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long schoolId;
    private String schoolName;
    private String device;
    private String browser;
    private String ipAddress;
    private String status;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
}
