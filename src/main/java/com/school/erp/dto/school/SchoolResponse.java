package com.school.erp.dto.school;

import java.time.LocalDateTime;

public record SchoolResponse(
        Long id,
        String name,
        String code,
        String contactEmail,
        String contactPhone,
        String address,
        String status,
        LocalDateTime createdAt,
        String city,
        String state,
        String pincode,
        String subdomain,
        String logoUrl,
        String bannerUrl
) {
}
