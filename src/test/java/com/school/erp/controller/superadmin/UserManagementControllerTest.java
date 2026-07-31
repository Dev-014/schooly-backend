package com.school.erp.controller.superadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.usermanagement.*;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.superadmin.UserManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = UserManagementController.class, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, AuthFilterConfig.class})
)
class UserManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserManagementService userManagementService;

    @Test
    void getDashboardStats_shouldReturnStats() throws Exception {
        UserDashboardStatsDTO stats = UserDashboardStatsDTO.builder()
                .totalUsers(100L)
                .activeUsers(90L)
                .build();
                
        when(userManagementService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/super-admin/users/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.activeUsers").value(90));
    }

    @Test
    void getLoginHistory_shouldReturnPage() throws Exception {
        UserLoginHistoryDTO history = UserLoginHistoryDTO.builder()
                .id(1L)
                .userName("Test User")
                .ipAddress("192.168.1.1")
                .status("SUCCESS")
                .build();
                
        Page<UserLoginHistoryDTO> page = new PageImpl<>(List.of(history), PageRequest.of(0, 10), 1);
        
        when(userManagementService.getLoginHistory(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/super-admin/users/login-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userName").value("Test User"))
                .andExpect(jsonPath("$.content[0].ipAddress").value("192.168.1.1"));
    }
}
