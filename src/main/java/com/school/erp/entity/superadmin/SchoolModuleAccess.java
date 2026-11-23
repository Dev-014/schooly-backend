package com.school.erp.entity.superadmin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "school_module_access")
public class SchoolModuleAccess {

    public SchoolModuleAccess(School school, PlatformModule module) {
        this.school = school;
        this.module = module;
        this.enabled = true;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private PlatformModule module;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "disabled_sub_modules", columnDefinition = "TEXT")
    private String disabledSubModules;

    public void setIsEnabled(Boolean isEnabled) {
        this.enabled = isEnabled;
    }

    public Boolean getIsEnabled() {
        return this.enabled;
    }

    @CreationTimestamp
    @Column(name = "enabled_at", insertable = false, updatable = false)
    private LocalDateTime enabledAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public java.util.List<String> getDisabledSubModulesList() {
        if (disabledSubModules == null || disabledSubModules.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        String cleaned = disabledSubModules.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
            if (cleaned.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            return java.util.Arrays.stream(cleaned.split(","))
                    .map(s -> s.trim().replaceAll("^\"|\"$", "").replaceAll("^'|'$", ""))
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    public void setDisabledSubModulesList(java.util.List<String> subs) {
        if (subs == null || subs.isEmpty()) {
            this.disabledSubModules = "[]";
        } else {
            java.util.List<String> quoted = subs.stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .map(s -> "\"" + s.trim().replace("\"", "\\\"") + "\"")
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            this.disabledSubModules = "[" + String.join(", ", quoted) + "]";
        }
    }
}
