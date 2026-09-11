package com.school.erp.controller;

import com.school.erp.dto.attendance.AttendanceResponse;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.AttendanceService;
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
}
