package com.school.erp.controller;

import com.school.erp.dto.attendance.BulkSubjectAttendanceRequest;
import com.school.erp.dto.attendance.StudentSubjectAttendanceSummaryDTO;
import com.school.erp.dto.attendance.SubjectAttendanceResponse;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.SubjectAttendanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SubjectAttendanceController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class SubjectAttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private SubjectAttendanceService subjectAttendanceService;


    @MockitoBean
    private AuthContextService authContextService;

    @Test
    void getSubjectAttendance_ForStudent_ShouldReturnList() throws Exception {
        SubjectAttendanceResponse resp = new SubjectAttendanceResponse(
                1L, 4L, 10L, "John Doe", 2L, "Grade 10", 3L, "Section A",
                5L, "Mathematics", "MATH-10", 12L, LocalDate.of(2026, 9, 11),
                "PRESENT", "Good work"
        );
        when(subjectAttendanceService.getStudentSubjectAttendance(4L, 10L)).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/attendance/subjects")
                        .param("schoolId", "4")
                        .param("studentId", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].subjectName").value("Mathematics"))
                .andExpect(jsonPath("$.data[0].status").value("PRESENT"));
    }

    @Test
    void getSubjectAttendance_ForPeriod_ShouldReturnList() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 11);
        SubjectAttendanceResponse resp = new SubjectAttendanceResponse(
                1L, 4L, 10L, "John Doe", 2L, "Grade 10", 3L, "Section A",
                5L, "Mathematics", "MATH-10", 12L, date,
                "PRESENT", null
        );
        when(subjectAttendanceService.getSubjectAttendanceByPeriod(4L, 2L, 3L, 5L, date))
                .thenReturn(List.of(resp));

        mockMvc.perform(get("/api/attendance/subjects")
                        .param("schoolId", "4")
                        .param("classId", "2")
                        .param("sectionId", "3")
                        .param("subjectId", "5")
                        .param("attendanceDate", "2026-09-11")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].subjectName").value("Mathematics"));
    }

    @Test
    void getStudentSubjectSummary_ShouldReturnSummaryList() throws Exception {
        StudentSubjectAttendanceSummaryDTO summary = new StudentSubjectAttendanceSummaryDTO(
                5L, "Mathematics", "MATH-10", 20, 18, 90
        );
        when(subjectAttendanceService.getStudentSubjectSummary(4L, 10L)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/attendance/subjects/summary")
                        .param("schoolId", "4")
                        .param("studentId", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].subjectName").value("Mathematics"))
                .andExpect(jsonPath("$.data[0].percentage").value(90));
    }

    @Test
    void createBulkSubjectAttendance_ShouldReturnCreatedRecords() throws Exception {
        BulkSubjectAttendanceRequest request = new BulkSubjectAttendanceRequest(
                4L, 2L, 3L, 5L, 12L, LocalDate.of(2026, 9, 11),
                List.of(new BulkSubjectAttendanceRequest.SubjectAttendanceEntry(10L, "PRESENT", null))
        );
        SubjectAttendanceResponse resp = new SubjectAttendanceResponse(
                1L, 4L, 10L, "John Doe", 2L, "Grade 10", 3L, "Section A",
                5L, "Mathematics", "MATH-10", 12L, LocalDate.of(2026, 9, 11),
                "PRESENT", null
        );
        when(subjectAttendanceService.saveBulkSubjectAttendance(any())).thenReturn(List.of(resp));

        mockMvc.perform(post("/api/attendance/subjects/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }
}
