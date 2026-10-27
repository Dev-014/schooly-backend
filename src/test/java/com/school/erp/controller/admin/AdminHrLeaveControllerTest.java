package com.school.erp.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.hr.StaffLeaveDTO;
import com.school.erp.dto.hr.StaffLeaveRequest;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.hr.StaffLeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminHrLeaveController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class AdminHrLeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private StaffLeaveService leaveService;

    @MockitoBean
    private AuthorizationService authorizationService;

    private StaffLeaveDTO sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = StaffLeaveDTO.builder()
                .id(1L)
                .schoolId(1000L)
                .staffId(2L)
                .staffName("John Doe")
                .leaveTypeId(1L)
                .leaveTypeName("Casual Leave")
                .startDate(LocalDate.of(2026, 10, 27))
                .endDate(LocalDate.of(2026, 10, 29))
                .numberOfDays(BigDecimal.valueOf(3))
                .reason("Attending conference")
                .status("PENDING")
                .build();
    }

    @Test
    void testGetAllLeaves_Success() throws Exception {
        when(leaveService.getAllLeaves(1000L)).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/v1/admin/schools/1000/hr/leaves")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].staffName").value("John Doe"))
                .andExpect(jsonPath("$[0].startDate").value("2026-10-27"))
                .andExpect(jsonPath("$[0].endDate").value("2026-10-29"))
                .andExpect(jsonPath("$[0].fromDate").value("2026-10-27"))
                .andExpect(jsonPath("$[0].toDate").value("2026-10-29"));
    }

    @Test
    void testApplyLeave_WithStartDateEndDate_Success() throws Exception {
        when(leaveService.applyLeave(eq(1000L), any(StaffLeaveRequest.class))).thenReturn(sampleDto);

        String payload = """
                {
                    "staffId": 2,
                    "leaveTypeId": 1,
                    "startDate": "2026-10-27",
                    "endDate": "2026-10-29",
                    "reason": "Attending conference"
                }
                """;

        mockMvc.perform(post("/api/v1/admin/schools/1000/hr/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.staffName").value("John Doe"))
                .andExpect(jsonPath("$.startDate").value("2026-10-27"))
                .andExpect(jsonPath("$.endDate").value("2026-10-29"));
    }

    @Test
    void testApplyLeave_WithFromDateToDateAliases_Success() throws Exception {
        when(leaveService.applyLeave(eq(1000L), any(StaffLeaveRequest.class))).thenReturn(sampleDto);

        String payload = """
                {
                    "staffId": 2,
                    "leaveTypeId": 1,
                    "fromDate": "2026-10-27",
                    "toDate": "2026-10-29",
                    "reason": "Attending conference",
                    "status": "PENDING"
                }
                """;

        mockMvc.perform(post("/api/v1/admin/schools/1000/hr/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fromDate").value("2026-10-27"))
                .andExpect(jsonPath("$.toDate").value("2026-10-29"));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        com.school.erp.security.AuthContextHolder.clear();
    }

    @Test
    void testApproveLeave_Success() throws Exception {
        com.school.erp.security.AuthContextHolder.set(
                new com.school.erp.security.AuthenticatedUser(1L, 1000L, com.school.erp.entity.auth.UserRole.ADMIN));
        when(authorizationService.hasPermission(eq(1000L), eq(1L), eq("leave_management.staff_leaves.edit"))).thenReturn(true);

        sampleDto.setStatus("APPROVED");
        sampleDto.setApprovedBy(99L);
        when(leaveService.approveLeave(eq(1000L), eq(1L), any(), eq("APPROVED"))).thenReturn(sampleDto);

        mockMvc.perform(put("/api/v1/admin/schools/1000/hr/leaves/1/approve")
                        .param("status", "APPROVED")
                        .param("adminStaffId", "99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void testApproveLeave_ForbiddenForTeacher() throws Exception {
        com.school.erp.security.AuthContextHolder.set(
                new com.school.erp.security.AuthenticatedUser(2L, 1000L, com.school.erp.entity.auth.UserRole.TEACHER));
        when(authorizationService.hasPermission(eq(1000L), eq(2L), eq("leave_management.staff_leaves.edit"))).thenReturn(false);

        mockMvc.perform(put("/api/v1/admin/schools/1000/hr/leaves/1/approve")
                        .param("status", "APPROVED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testApproveLeave_PermittedForCustomRoleWithPermission() throws Exception {
        // Custom role with archetype STAFF (e.g. HR Manager) granted staff_leaves.edit permission
        com.school.erp.security.AuthContextHolder.set(
                new com.school.erp.security.AuthenticatedUser(99L, 1000L, com.school.erp.entity.auth.UserRole.STAFF));
        when(authorizationService.hasPermission(eq(1000L), eq(99L), eq("leave_management.staff_leaves.edit"))).thenReturn(true);

        sampleDto.setStatus("APPROVED");
        sampleDto.setApprovedBy(99L);
        when(leaveService.approveLeave(eq(1000L), eq(1L), any(), eq("APPROVED"))).thenReturn(sampleDto);

        mockMvc.perform(put("/api/v1/admin/schools/1000/hr/leaves/1/approve")
                        .param("status", "APPROVED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
