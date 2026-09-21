package com.school.erp.controller;

import com.school.erp.dto.attendance.AttendanceResponse;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.controller.attendance.AttendanceController;
import com.school.erp.service.attendance.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AttendanceController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private AuthContextService authContextService;

    @Test
    void getAttendance_WithStudentId_ShouldReturnAttendanceList() throws Exception {
        AttendanceResponse resp = new AttendanceResponse(1L, 1L, 4L, LocalDate.of(2026, 9, 11), "PRESENT");
        when(attendanceService.getAttendance(4L, 1L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/attendance")
                        .param("schoolId", "4")
                        .param("studentId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].studentId").value(1L))
                .andExpect(jsonPath("$.data[0].status").value("PRESENT"));
    }

    @Test
    void getAttendanceDaily_WithOnlyStudentId_ShouldGracefullyFallbackToStudentAttendance() throws Exception {
        AttendanceResponse resp = new AttendanceResponse(1L, 1L, 4L, LocalDate.of(2026, 9, 11), "PRESENT");
        when(attendanceService.getAttendance(4L, 1L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/attendance/daily")
                        .param("schoolId", "4")
                        .param("studentId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].studentId").value(1L))
                .andExpect(jsonPath("$.data[0].status").value("PRESENT"));
    }

    @Test
    void getAttendanceDaily_WithClassAndDate_ShouldReturnDailyAttendance() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 11);
        AttendanceResponse resp = new AttendanceResponse(2L, 5L, 4L, date, "PRESENT");
        when(attendanceService.getAttendanceByDate(4L, 10L, 20L, date)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/attendance/daily")
                        .param("schoolId", "4")
                        .param("classId", "10")
                        .param("sectionId", "20")
                        .param("attendanceDate", "2026-09-11")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(2L))
                .andExpect(jsonPath("$.data[0].studentId").value(5L));
    }

    @Test
    void getSummaryToday_ShouldReturnSummaryDto() throws Exception {
        com.school.erp.dto.attendance.AttendanceSummaryDTO summary =
                new com.school.erp.dto.attendance.AttendanceSummaryDTO(100, 90, 5, 5, 95, 2);
        when(attendanceService.getSummaryToday(4L, null)).thenReturn(summary);

        mockMvc.perform(get("/api/attendance/summary/today")
                        .param("schoolId", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.totalStudents").value(100))
                .andExpect(jsonPath("$.data.presentPercent").value(95))
                .andExpect(jsonPath("$.data.pendingLeaves").value(2));
    }

    @Test
    void getAttendanceTrends_ShouldReturnTrendList() throws Exception {
        com.school.erp.dto.attendance.analytics.AttendanceTrendDTO trend =
                new com.school.erp.dto.attendance.analytics.AttendanceTrendDTO(LocalDate.of(2026, 9, 11), 94);
        when(attendanceService.getAttendanceTrends(4L, 30, null)).thenReturn(List.of(trend));

        mockMvc.perform(get("/api/attendance/analytics/trend")
                        .param("schoolId", "4")
                        .param("days", "30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].presentPercent").value(94));
    }

    @Test
    void getGradeWiseAttendance_ShouldReturnGradeWiseList() throws Exception {
        com.school.erp.dto.attendance.analytics.GradeAttendanceDTO grade =
                new com.school.erp.dto.attendance.analytics.GradeAttendanceDTO("Grade 10", 35, 94.2, 2.5, "EXCELLENT");
        when(attendanceService.getGradeWiseAttendance(4L)).thenReturn(List.of(grade));

        mockMvc.perform(get("/api/attendance/analytics/grade-wise")
                        .param("schoolId", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].className").value("Grade 10"))
                .andExpect(jsonPath("$.data[0].avgAttendance").value(94.2))
                .andExpect(jsonPath("$.data[0].performance").value("EXCELLENT"));
    }
}
