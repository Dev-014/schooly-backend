package com.school.erp.controller.finance;

import com.school.erp.dto.payment.FeeStatsDTO;
import com.school.erp.dto.payment.StudentFeeRecordDTO;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.finance.FeeRecordService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = FeeRecordController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class FeeRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeeRecordService feeRecordService;

    @MockitoBean
    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        AuthContextHolder.set(new AuthenticatedUser(100L, 1L, UserRole.TEACHER));
        when(authorizationService.hasPermission(eq(1L), eq(100L), eq("fees.search_due_fees.view"))).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        AuthContextHolder.clear();
    }

    @Test
    void getFeeRecords_ReturnsRecordsSuccessfully() throws Exception {
        StudentFeeRecordDTO record = new StudentFeeRecordDTO(
                "1", "1", "Aarav Sharma", "ADM-001", "Class 10", null,
                new BigDecimal("25000.00"), new BigDecimal("15000.00"), new BigDecimal("10000.00"), null, "PARTIAL"
        );
        when(feeRecordService.getStudentFeeRecords(eq(1L), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(record)));

        mockMvc.perform(get("/api/v1/finance/fee-records")
                        .param("schoolId", "1")
                        .param("classGroupId", "5")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content[0].studentName").value("Aarav Sharma"))
                .andExpect(jsonPath("$.data.content[0].balance").value(10000.00));
    }

    @Test
    void getFeeStats_ReturnsStatsSuccessfully() throws Exception {
        FeeStatsDTO stats = new FeeStatsDTO(
                new BigDecimal("500000.00"),
                new BigDecimal("350000.00"),
                BigDecimal.ZERO,
                new BigDecimal("150000.00"),
                0L
        );
        when(feeRecordService.getFeeStats(eq(1L), eq(5L))).thenReturn(stats);

        mockMvc.perform(get("/api/v1/finance/fee-records/stats")
                        .param("schoolId", "1")
                        .param("classGroupId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalExpected").value(500000.00))
                .andExpect(jsonPath("$.data.overdueBalance").value(150000.00));
    }
}
