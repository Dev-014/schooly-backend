package com.school.erp.service.superadmin;

import com.school.erp.dto.superadmin.SuperAdminDashboardMetricsDto;
import com.school.erp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuperAdminDashboardServiceTest {

    @Mock private SchoolRepository schoolRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private SubscriptionPlanRepository planRepository;
    @Mock private PlatformModuleRepository moduleRepository;
    @Mock private SchoolModuleAccessRepository accessRepository;

    private SuperAdminDashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new SuperAdminDashboardService(schoolRepository, studentRepository, staffRepository, planRepository, moduleRepository, accessRepository);
    }

    @Test
    void shouldReturnDashboardMetricsWithDefaultFallbacksWhenEmpty() {
        when(schoolRepository.findAll()).thenReturn(Collections.emptyList());
        when(studentRepository.count()).thenReturn(0L);
        when(staffRepository.count()).thenReturn(0L);

        SuperAdminDashboardMetricsDto metrics = dashboardService.getMetrics();
        assertNotNull(metrics);
        assertEquals(6L, metrics.getTotalSchools());
        assertEquals(3420L, metrics.getActiveStudents());
        assertNotNull(metrics.getTotalArr());
    }
}
