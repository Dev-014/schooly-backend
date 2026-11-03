package com.school.erp.controller.common;

import com.school.erp.dto.dashboard.CollectionExpensePointResponse;
import com.school.erp.dto.dashboard.DashboardBreakdownResponse;
import com.school.erp.dto.dashboard.DashboardKpiResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.common.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DashboardController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private AuthorizationService authorizationService;

    @Test
    void testGetKpisReturnsOk() throws Exception {
        DashboardKpiResponse kpi = new DashboardKpiResponse(
                new BigDecimal("100000.00"),
                new BigDecimal("30000.00"),
                new BigDecimal("31500.00"),
                15,
                20,
                22,
                350,
                400,
                "+12%",
                new BigDecimal("25000.00")
        );
        when(dashboardService.getKpis(any())).thenReturn(kpi);

        mockMvc.perform(get("/api/dashboard/kpis").param("schoolId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.monthlyCollection").value(100000.00))
                .andExpect(jsonPath("$.data.monthlyExpense").value(30000.00))
                .andExpect(jsonPath("$.data.monthlyIncome").value(25000.00))
                .andExpect(jsonPath("$.data.studentPresent").value(350))
                .andExpect(jsonPath("$.data.newAdmissions").value(15));
    }

    @Test
    void testGetEarningModesReturnsOk() throws Exception {
        var items = List.of(
                new DashboardBreakdownResponse.EarningModeItem("Online", new BigDecimal("75000.00"), 75.0),
                new DashboardBreakdownResponse.EarningModeItem("Cash/Other", new BigDecimal("25000.00"), 25.0)
        );
        when(dashboardService.getEarningModes(any())).thenReturn(items);

        mockMvc.perform(get("/api/dashboard/earning-modes").param("schoolId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].mode").value("Online"))
                .andExpect(jsonPath("$.data[0].percentage").value(75.0));
    }

    @Test
    void testGetFeeDistributionReturnsOk() throws Exception {
        var items = List.of(
                new DashboardBreakdownResponse.FeeDistributionItem("Tuition", new BigDecimal("60000.00"), 60.0)
        );
        when(dashboardService.getFeeDistribution(any())).thenReturn(items);

        mockMvc.perform(get("/api/dashboard/fee-distribution").param("schoolId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].category").value("Tuition"));
    }

    @Test
    void testGetClassDistributionReturnsOk() throws Exception {
        var items = List.of(
                new DashboardBreakdownResponse.ClassDistributionItem(1L, "Class 1", 25L, 50.0)
        );
        when(dashboardService.getClassDistribution(any())).thenReturn(items);

        mockMvc.perform(get("/api/dashboard/class-distribution").param("schoolId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].className").value("Class 1"));
    }

    @Test
    void testGetStudentBirthdaysReturnsOk() throws Exception {
        var items = List.of(
                new DashboardBreakdownResponse.StudentBirthdayItem(10L, "John Smith", "Class 5", "2015-11-02")
        );
        when(dashboardService.getStudentBirthdays(any())).thenReturn(items);

        mockMvc.perform(get("/api/dashboard/student-birthdays").param("schoolId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].studentName").value("John Smith"));
    }

    @Test
    void testGetCollectionVsExpensesReturnsOk() throws Exception {
        var points = List.of(
                new CollectionExpensePointResponse("JAN", new BigDecimal("50000.00"), new BigDecimal("20000.00"))
        );
        when(dashboardService.getCollectionVsExpense(any(), anyInt())).thenReturn(points);

        mockMvc.perform(get("/api/dashboard/chart/collection-vs-expenses").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].month").value("JAN"));
    }
}

