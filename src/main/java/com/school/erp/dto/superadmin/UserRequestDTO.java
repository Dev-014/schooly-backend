package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long schoolId;
    private String schoolName;
    private String requestType;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
