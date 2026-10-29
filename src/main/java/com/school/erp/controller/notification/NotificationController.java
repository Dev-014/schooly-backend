package com.school.erp.controller.notification;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.notification.BatchNotificationActionRequest;
import com.school.erp.dto.notification.CreateNotificationRequest;
import com.school.erp.dto.notification.NotificationCountResponse;
import com.school.erp.dto.notification.NotificationResponse;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Multi-role notification system for tracking, reading, and dispatching notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthContextService authContextService;

    @GetMapping
    @Operation(summary = "Get user notifications with filtering and pagination")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String targetRole,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) NotificationCategory category,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AuthenticatedUser user = authContextService.requireCurrentUser();
        Long resolvedSchoolId = authContextService.resolveSchoolId(schoolId);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));

        Page<NotificationResponse> notificationPage = notificationService.getNotifications(
                user.userId(),
                resolvedSchoolId,
                targetRole,
                isRead,
                category,
                type,
                priority,
                pageable
        );

        PaginationMeta paginationMeta = new PaginationMeta(
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements()
        );

        return ResponseEntity.ok(ApiResponse.success(
                notificationPage.getContent(),
                "Notifications retrieved successfully",
                paginationMeta
        ));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get total unread count and category breakdown for current user")
    public ResponseEntity<ApiResponse<NotificationCountResponse>> getUnreadSummary(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String targetRole
    ) {
        AuthenticatedUser user = authContextService.requireCurrentUser();
        Long resolvedSchoolId = authContextService.resolveSchoolId(schoolId);

        NotificationCountResponse response = notificationService.getUnreadSummary(
                user.userId(), resolvedSchoolId, targetRole);
        return ResponseEntity.ok(ApiResponse.success(response, "Unread count fetched successfully"));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a single notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        AuthenticatedUser user = authContextService.requireCurrentUser();
        Long resolvedSchoolId = authContextService.resolveSchoolId(schoolId);

        NotificationResponse response = notificationService.markAsRead(id, user.userId(), resolvedSchoolId);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification marked as read"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read for current user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAllAsRead(
            @RequestParam(required = false) Long schoolId
    ) {
        AuthenticatedUser user = authContextService.requireCurrentUser();
        Long resolvedSchoolId = authContextService.resolveSchoolId(schoolId);

        int count = notificationService.markAllAsRead(user.userId(), resolvedSchoolId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("updatedCount", count),
                "All notifications marked as read"
        ));
    }

    @PutMapping("/batch-read")
    @Operation(summary = "Mark a batch of selected notifications as read")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markBatchAsRead(
            @Valid @RequestBody BatchNotificationActionRequest request,
            @RequestParam(required = false) Long schoolId
    ) {
        AuthenticatedUser user = authContextService.requireCurrentUser();
        Long resolvedSchoolId = authContextService.resolveSchoolId(schoolId);

        int count = notificationService.markBatchAsRead(request.notificationIds(), user.userId(), resolvedSchoolId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("updatedCount", count),
                "Batch notifications marked as read"
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dismiss / delete a single notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        AuthenticatedUser user = authContextService.requireCurrentUser();
        Long resolvedSchoolId = authContextService.resolveSchoolId(schoolId);

        notificationService.deleteNotification(id, user.userId(), resolvedSchoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification dismissed successfully"));
    }

    @DeleteMapping("/clear-read")
    @Operation(summary = "Delete all read notifications for current user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearReadNotifications(
            @RequestParam(required = false) Long schoolId
    ) {
        AuthenticatedUser user = authContextService.requireCurrentUser();
        Long resolvedSchoolId = authContextService.resolveSchoolId(schoolId);

        int deletedCount = notificationService.clearReadNotifications(user.userId(), resolvedSchoolId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("deletedCount", deletedCount),
                "Read notifications cleared successfully"
        ));
    }

    @PostMapping
    @Operation(summary = "Dispatch / send notification to a user or broadcast to a role")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request,
            @RequestParam(required = false) Long schoolId
    ) {
        AuthenticatedUser user = authContextService.requireCurrentUser();
        Long resolvedSchoolId = authContextService.resolveSchoolId(schoolId);

        List<NotificationResponse> responses = notificationService.createNotification(request, resolvedSchoolId, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                responses,
                "Notification(s) dispatched successfully"
        ));
    }
}
