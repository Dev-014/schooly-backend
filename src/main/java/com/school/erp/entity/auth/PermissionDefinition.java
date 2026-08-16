package com.school.erp.entity.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "permission_definitions")
public class PermissionDefinition {

    @Id
    private String id;

    @Column(name = "permission_key", nullable = false, unique = true)
    private String permissionKey;

    @Column(name = "module_key", nullable = false)
    private String moduleKey;

    @Column(name = "resource_key", nullable = false)
    private String resourceKey;

    @Column(name = "action_key", nullable = false)
    private String actionKey;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "supported_scope_types", columnDefinition = "jsonb")
    private java.util.List<String> supportedScopeTypes;

    @Column(name = "requires_assignment", nullable = false)
    private boolean requiresAssignment = false;

    @Column(name = "is_sensitive", nullable = false)
    private boolean isSensitive = false;

    @Column(name = "is_system_permission", nullable = false)
    private boolean isSystemPermission = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
