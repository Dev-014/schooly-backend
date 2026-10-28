package com.school.erp.service.notification;

import com.school.erp.dto.notification.CreateNotificationRequest;
import com.school.erp.dto.notification.NotificationCountResponse;
import com.school.erp.dto.notification.NotificationResponse;
import com.school.erp.entity.auth.User;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.auth.UserSchoolRole;
import com.school.erp.entity.notification.Notification;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.repository.auth.UserRepository;
import com.school.erp.repository.auth.UserSchoolRoleRepository;
import com.school.erp.repository.notification.NotificationRepository;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.notification.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserSchoolRoleRepository userSchoolRoleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = Notification.builder()
                .id(1L)
                .schoolId(1L)
                .userId(7L)
                .targetRole("PARENT")
                .title("New Homework: Mathematics")
                .message("Exercise 4.2 due tomorrow")
                .notificationType(NotificationType.HOMEWORK_ASSIGNED)
                .category(NotificationCategory.ACADEMIC)
                .priority(NotificationPriority.NORMAL)
                .isRead(false)
                .actorId(3L)
                .actorRole("TEACHER")
                .actorName("Sarah Teacher")
                .actionUrl("/homework")
                .actionType("NAVIGATE")
                .entityType("HOMEWORK")
                .entityId(101L)
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();
    }

    @Test
    void getNotifications_ReturnsFilteredPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> mockPage = new PageImpl<>(List.of(sampleNotification), pageable, 1);

        when(notificationRepository.filterNotifications(
                eq(1L), eq(7L), eq(false), eq(NotificationCategory.ACADEMIC),
                eq(NotificationType.HOMEWORK_ASSIGNED), eq(NotificationPriority.NORMAL), eq(pageable)
        )).thenReturn(mockPage);

        Page<NotificationResponse> result = notificationService.getNotifications(
                7L, 1L, false, NotificationCategory.ACADEMIC,
                NotificationType.HOMEWORK_ASSIGNED, NotificationPriority.NORMAL, pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        NotificationResponse dto = result.getContent().get(0);
        assertEquals("New Homework: Mathematics", dto.title());
        assertEquals("Sarah Teacher", dto.actorName());
        assertEquals("PARENT", dto.targetRole());
        assertEquals(NotificationType.HOMEWORK_ASSIGNED, dto.notificationType());
    }

    @Test
    void getUnreadSummary_ReturnsAccurateCounts() {
        when(notificationRepository.countBySchoolIdAndUserIdAndIsReadFalse(1L, 7L)).thenReturn(4L);
        when(notificationRepository.countBySchoolIdAndUserId(1L, 7L)).thenReturn(10L);
        when(notificationRepository.countUnreadGroupedByCategory(1L, 7L)).thenReturn(List.of(
                new Object[]{"ACADEMIC", 2L},
                new Object[]{"FINANCE", 1L},
                new Object[]{"ATTENDANCE", 1L}
        ));

        NotificationCountResponse response = notificationService.getUnreadSummary(7L, 1L);

        assertEquals(4L, response.unreadCount());
        assertEquals(10L, response.totalCount());
        assertEquals(2L, response.unreadByCategory().get("ACADEMIC"));
        assertEquals(1L, response.unreadByCategory().get("FINANCE"));
        assertEquals(1L, response.unreadByCategory().get("ATTENDANCE"));
    }

    @Test
    void markAsRead_UpdatesIsReadAndReadAt() {
        when(notificationRepository.findByIdAndSchoolIdAndUserId(1L, 1L, 7L))
                .thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(1L, 7L, 1L);

        assertTrue(response.isRead());
        assertNotNull(response.readAt());
        verify(notificationRepository).save(sampleNotification);
    }

    @Test
    void markAllAsRead_ExecutesRepositoryBulkUpdate() {
        when(notificationRepository.markAllAsRead(eq(1L), eq(7L), any(LocalDateTime.class)))
                .thenReturn(5);

        int count = notificationService.markAllAsRead(7L, 1L);

        assertEquals(5, count);
        verify(notificationRepository).markAllAsRead(eq(1L), eq(7L), any(LocalDateTime.class));
    }

    @Test
    void markBatchAsRead_ExecutesBatchUpdate() {
        when(notificationRepository.markBatchAsRead(eq(1L), eq(7L), eq(List.of(1L, 2L)), any(LocalDateTime.class)))
                .thenReturn(2);

        int count = notificationService.markBatchAsRead(List.of(1L, 2L), 7L, 1L);

        assertEquals(2, count);
    }

    @Test
    void createNotification_DirectToUser() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                7L,
                "PARENT",
                "Fee Reminder",
                "Term 2 fee is due",
                NotificationType.FEE_DUE_REMINDER,
                NotificationCategory.FINANCE,
                NotificationPriority.HIGH,
                "/finance/fees",
                "NAVIGATE",
                "FEE_INVOICE",
                501L,
                Map.of("amount", 14500)
        );

        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(99L);
            return n;
        });

        AuthenticatedUser admin = new AuthenticatedUser(2L, 1L, UserRole.ADMIN);
        List<NotificationResponse> responses = notificationService.createNotification(request, 1L, admin);

        assertEquals(1, responses.size());
        NotificationResponse res = responses.get(0);
        assertEquals("Fee Reminder", res.title());
        assertEquals(NotificationType.FEE_DUE_REMINDER, res.notificationType());
        assertEquals(NotificationCategory.FINANCE, res.category());
        assertEquals("HIGH", res.priority().name());
    }

    @Test
    void createNotification_BroadcastToRole() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                null,
                "PARENT",
                "School Holiday Alert",
                "School will remain closed tomorrow",
                NotificationType.CIRCULAR_ANNOUNCEMENT,
                NotificationCategory.COMMUNICATION,
                NotificationPriority.HIGH,
                "/communication",
                "NAVIGATE",
                "CIRCULAR",
                202L,
                Map.of()
        );

        User p1 = new User();
        p1.setId(7L);
        UserSchoolRole role1 = new UserSchoolRole();
        role1.setUser(p1);

        User p2 = new User();
        p2.setId(10L);
        UserSchoolRole role2 = new UserSchoolRole();
        role2.setUser(p2);

        when(userSchoolRoleRepository.findBySchoolIdAndRoleAndStatusIgnoreCase(1L, UserRole.PARENT, "ACTIVE"))
                .thenReturn(List.of(role1, role2));

        when(notificationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        AuthenticatedUser admin = new AuthenticatedUser(2L, 1L, UserRole.ADMIN);
        List<NotificationResponse> responses = notificationService.createNotification(request, 1L, admin);

        assertEquals(2, responses.size());
        verify(notificationRepository).saveAll(anyList());
    }
}
