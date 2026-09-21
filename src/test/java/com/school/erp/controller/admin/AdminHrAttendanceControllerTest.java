package com.school.erp.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.hr.BulkStaffAttendanceRequest;
import com.school.erp.dto.hr.StaffAttendanceDTO;
import com.school.erp.dto.hr.StaffAttendanceRequest;
import com.school.erp.dto.hr.StaffAttendanceStatsDTO;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.hr.StaffAttendanceService;
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
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminHrAttendanceController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class AdminHrAttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private StaffAttendanceService attendanceService;

    @MockitoBean
    private AuthorizationService authorizationService;

    private StaffAttendanceDTO sampleDto;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2026, 9, 13);
        com.school.erp.security.AuthContextHolder.set(
                new com.school.erp.security.AuthenticatedUser(100L, 1L, com.school.erp.entity.auth.UserRole.ADMIN)
        );
        when(authorizationService.hasPermission(any(), any(), any())).thenReturn(true);

        sampleDto = StaffAttendanceDTO.builder()
                .id(1L)
                .schoolId(1L)
                .staffId(10L)
                .staffName("Arthur Morgan")
                .staffCode("STF-0010")
                .department("History")
                .designation("Senior Teacher")
                .attendanceDate(today)
                .status("PRESENT")
                .checkInTime(LocalTime.of(8, 0))
                .checkOutTime(LocalTime.of(16, 30))
                .workingHours(new BigDecimal("8.50"))
                .build();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        com.school.erp.security.AuthContextHolder.clear();
    }

    @Test
    void testGetAttendanceRegister() throws Exception {
        when(attendanceService.getAttendanceRegister(eq(1L), eq(today), isNull(), isNull()))
                .thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/v1/admin/schools/1/hr/attendance/register")
                        .param("date", "2026-09-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].staffName").value("Arthur Morgan"))
                .andExpect(jsonPath("$[0].status").value("PRESENT"));
    }

    @Test
    void testGetAttendanceStats() throws Exception {
        StaffAttendanceStatsDTO stats = StaffAttendanceStatsDTO.builder()
                .date(today)
                .totalStaff(10)
                .presentCount(8)
                .absentCount(1)
                .lateCount(1)
                .halfDayCount(0)
                .onLeaveCount(0)
                .unmarkedCount(0)
                .attendancePercentage(90.0)
                .build();

        when(attendanceService.getAttendanceStats(eq(1L), eq(today)))
                .thenReturn(stats);

        mockMvc.perform(get("/api/v1/admin/schools/1/hr/attendance/stats")
                        .param("date", "2026-09-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStaff").value(10))
                .andExpect(jsonPath("$.presentCount").value(8))
                .andExpect(jsonPath("$.attendancePercentage").value(90.0));
    }

    @Test
    void testMarkAttendance() throws Exception {
        StaffAttendanceRequest req = new StaffAttendanceRequest();
        req.setStaffId(10L);
        req.setAttendanceDate(today);
        req.setStatus("PRESENT");
        req.setCheckInTime(LocalTime.of(8, 0));

        when(attendanceService.markAttendance(eq(1L), any(StaffAttendanceRequest.class)))
                .thenReturn(sampleDto);

        mockMvc.perform(post("/api/v1/admin/schools/1/hr/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.staffName").value("Arthur Morgan"));
    }

    @Test
    void testBulkMarkAttendance() throws Exception {
        StaffAttendanceRequest req = new StaffAttendanceRequest();
        req.setStaffId(10L);
        req.setAttendanceDate(today);
        req.setStatus("PRESENT");

        BulkStaffAttendanceRequest bulkReq = BulkStaffAttendanceRequest.builder()
                .attendanceDate(today)
                .records(List.of(req))
                .build();

        when(attendanceService.bulkMarkAttendance(eq(1L), any(BulkStaffAttendanceRequest.class)))
                .thenReturn(List.of(sampleDto));

        mockMvc.perform(post("/api/v1/admin/schools/1/hr/attendance/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].staffName").value("Arthur Morgan"));
    }
}
