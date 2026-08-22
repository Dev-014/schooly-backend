package com.school.erp.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RoleDto {
    private String id;
    private String name;
    private String code;
    private String description;
    @JsonProperty("isSystemRole")
    private boolean isSystemRole;
    private int userCount;
}
