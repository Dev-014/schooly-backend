package com.school.erp.dto.auth;

import lombok.Data;
import java.util.List;

@Data
public class UpdateRolePermissionsRequest {
    private List<PermissionUpdateDto> permissions;

    @Data
    public static class PermissionUpdateDto {
        private String permissionKey;
        private boolean granted;
        private String scope;
    }
}
