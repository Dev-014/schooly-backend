package com.school.erp.dto.usermanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpersonationResponseDTO {
    private String accessToken;
    private String refreshToken;
    private Long targetUserId;
    private String targetUserName;
    private String targetUserRole;
    private String sessionId;
}
