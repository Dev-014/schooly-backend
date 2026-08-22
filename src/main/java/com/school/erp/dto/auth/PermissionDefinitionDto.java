package com.school.erp.dto.auth;

import lombok.Data;
import java.util.List;

@Data
public class PermissionDefinitionDto {
    private String id;
    private String key;
    private String name;
    private String description;
    private boolean isSensitive;
    private boolean granted;
    private String scope;
    private List<String> availableScopes;
}
