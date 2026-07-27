package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "platform_modules")
public class PlatformModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category = "CORE";

    @Column(name = "add_on_price", precision = 10, scale = 2)
    private java.math.BigDecimal addOnPrice = java.math.BigDecimal.ZERO;

    @Column(name = "target_roles")
    private String targetRoles = "ADMIN";

    @Column(name = "sub_modules", columnDefinition = "TEXT")
    private String subModules;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    public boolean isDefault() {
        return Boolean.TRUE.equals(isDefault);
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public List<String> getTargetRolesList() {
        if (targetRoles == null || targetRoles.trim().isEmpty()) {
            return Arrays.asList("ADMIN");
        }
        return Arrays.stream(targetRoles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public void setTargetRolesList(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            this.targetRoles = "ADMIN";
        } else {
            this.targetRoles = String.join(",", roles);
        }
    }

    public List<String> getSubModulesList() {
        if (subModules == null || subModules.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String cleaned = subModules.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
            return Arrays.stream(cleaned.split(","))
                    .map(s -> s.trim().replaceAll("^\"|\"$", "").replaceAll("^'|'$", ""))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return Arrays.stream(subModules.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public void setSubModulesList(List<String> subs) {
        if (subs == null || subs.isEmpty()) {
            this.subModules = "[]";
        } else {
            List<String> quoted = subs.stream().map(s -> "\"" + s.replace("\"", "\\\"") + "\"").collect(Collectors.toList());
            this.subModules = "[" + String.join(", ", quoted) + "]";
        }
    }

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
