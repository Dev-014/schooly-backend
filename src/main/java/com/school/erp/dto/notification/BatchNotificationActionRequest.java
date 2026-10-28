package com.school.erp.dto.notification;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchNotificationActionRequest(
        @NotEmpty(message = "Notification IDs list must not be empty")
        List<Long> notificationIds
) {
}
