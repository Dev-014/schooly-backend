package com.school.erp.dto.dashboard;

import java.time.LocalDateTime;

public record ActivityFeedResponse(
        String id,
        String type,
        String title,
        String description,
        LocalDateTime timestamp,
        String severity
) {
}
