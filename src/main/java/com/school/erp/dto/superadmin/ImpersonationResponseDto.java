package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImpersonationResponseDto {
    private String token;
    private String expiresAt;
    private String schoolName;
    private String[] permissions;
}
