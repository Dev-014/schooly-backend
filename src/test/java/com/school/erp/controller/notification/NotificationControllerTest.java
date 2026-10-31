package com.school.erp.controller.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.notification.BatchNotificationActionRequest;
import com.school.erp.dto.notification.CreateNotificationRequest;
import com.school.erp.dto.notification.NotificationCountResponse;
import com.school.erp.dto.notification.NotificationResponse;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = NotificationController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private AuthContextService authContextService;

    private AuthenticatedUser mockUser;
    private NotificationResponse mockNotificationResponse;

    @BeforeEach
    void setUp() {
        mockUser = new AuthenticatedUser(7L, 1L, UserRole.PARENT);
        when(authContextService.requireCurrentUser()).thenReturn(mockUser);
        when(authContextService.resolveSchoolId(any())).thenReturn(1L);

        mockNotificationResponse = new NotificationResponse(
                1L, 1L, 7L, "PARENT",
                "New Homework: Mathematics",
                "Exercise 4.2 due tomorrow",
                NotificationType.HOMEWORK_ASSIGNED,
                NotificationCategory.ACADEMIC,
                NotificationPriority.NORMAL,
                false,
                null,
                3L,
                "TEACHER",
                "Sarah Teacher",
                "/homework",
                "NAVIGATE",
                "HOMEWORK",
                101L,
                Map.of(),
                LocalDateTime.now(),
                "just now"
        );
    }

    @Test
    void getNotifications_ReturnsListWithPagination() throws Exception {
        when(notificationService.getNotifications(eq(7L), eq(1L), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockNotificationResponse)));

        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("New Homework: Mathematics"))
                .andExpect(jsonPath("$.data[0].notificationType").value("HOMEWORK_ASSIGNED"))
                .andExpect(jsonPath("$.data[0].actorName").value("Sarah Teacher"))
                .andExpect(jsonPath("$.pagination.total").value(1));
    }

    @Test
    void getUnreadSummary_ReturnsCounts() throws Exception {
        NotificationCountResponse countResponse = new NotificationCountResponse(3L, 8L, Map.of("ACADEMIC", 2L, "FINANCE", 1L));
        when(notificationService.getUnreadSummary(7L, 1L)).thenReturn(countResponse);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.unreadCount").value(3))
                .andExpect(jsonPath("$.data.totalCount").value(8))
                .andExpect(jsonPath("$.data.unreadByCategory.ACADEMIC").value(2));
    }

    @Test
    void markAsRead_ReturnsUpdatedNotification() throws Exception {
        NotificationResponse readResponse = new NotificationResponse(
                1L, 1L, 7L, "PARENT",
                "New Homework: Mathematics",
                "Exercise 4.2 due tomorrow",
                NotificationType.HOMEWORK_ASSIGNED,
                NotificationCategory.ACADEMIC,
                NotificationPriority.NORMAL,
                true,
                LocalDateTime.now(),
                3L,
                "TEACHER",
                "Sarah Teacher",
                "/homework",
                "NAVIGATE",
                "HOMEWORK",
                101L,
                Map.of(),
                LocalDateTime.now(),
                "just now"
        );
        when(notificationService.markAsRead(1L, 7L, 1L)).thenReturn(readResponse);

        mockMvc.perform(put("/api/notifications/1/read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.isRead").value(true));
    }

    @Test
    void markAllAsRead_ReturnsUpdatedCount() throws Exception {
        when(notificationService.markAllAsRead(7L, 1L)).thenReturn(4);

        mockMvc.perform(put("/api/notifications/read-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.updatedCount").value(4));
    }

    @Test
    void markBatchAsRead_ReturnsBatchCount() throws Exception {
        when(notificationService.markBatchAsRead(eq(List.of(1L, 2L)), eq(7L), eq(1L))).thenReturn(2);

        BatchNotificationActionRequest request = new BatchNotificationActionRequest(List.of(1L, 2L));

        mockMvc.perform(put("/api/notifications/batch-read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.updatedCount").value(2));
    }

    @Test
    void deleteNotification_ReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/notifications/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void createNotification_DispatchesSuccessfully() throws Exception {
        CreateNotificationRequest request = new CreateNotificationRequest(
                7L,
                "PARENT",
                "Exam Alert",
                "Dates announced",
                NotificationType.EXAM_SCHEDULE_PUBLISHED,
                NotificationCategory.ACADEMIC,
                NotificationPriority.NORMAL,
                "/exams",
                "NAVIGATE",
                "EXAM",
                1L,
                Map.of()
        );

        when(notificationService.createNotification(any(), eq(1L), eq(mockUser)))
                .thenReturn(List.of(mockNotificationResponse));

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }
}
