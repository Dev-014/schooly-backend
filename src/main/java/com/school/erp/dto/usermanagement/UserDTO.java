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
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private Long schoolId;
    private String schoolName;
    private LocalDateTime lastLogin;
    private String employeeId;
}
