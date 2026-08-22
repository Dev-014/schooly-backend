package com.school.erp.dto.auth;

import lombok.Data;
import java.util.List;

@Data
public class PermissionGroupDto {
    private String module;
    private List<PermissionDefinitionDto> permissions;
}
