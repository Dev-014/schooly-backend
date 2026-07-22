package com.school.erp.dto.superadmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDto {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String contactEmail;
    private String contactPhone;
    private String status;
    private String subdomain;
    private String domain;
    private String plan;
    private Integer studentCount;
    private Integer staffCount;
    private Integer modulesCount;
    private String healthStatus;
    private String onboardingStep;
    private String createdAt;
    private Map<String, Object> metadata;

    // Backward compatible constructor for existing calls
    public SchoolDto(Long id, String name, String code, String address, String contactEmail, String contactPhone, String status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.address = address;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.status = status;
    }
}
