package com.school.erp.dto.notification;

import java.util.Map;

public record NotificationCountResponse(
        long unreadCount,
        long totalCount,
        Map<String, Long> unreadByCategory
) {
}
