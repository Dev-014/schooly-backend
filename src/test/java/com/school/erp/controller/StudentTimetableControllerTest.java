package com.school.erp.controller;

import com.school.erp.dto.academic.TimetableEntryResponse;
import com.school.erp.dto.academic.TimetablePeriodResponse;
import com.school.erp.dto.student.StudentTimetableResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.StudentTimetableService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StudentTimetableController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class StudentTimetableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentTimetableService studentTimetableService;

    @Test
    void getTimetable_ShouldReturnStudentTimetableResponse() throws Exception {
        List<TimetablePeriodResponse> periods = List.of(
                new TimetablePeriodResponse(1L, 1L, 1, "Period 1", "08:00", "08:45", false),
                new TimetablePeriodResponse(2L, 1L, 2, "Period 2", "08:50", "09:35", false)
        );

        List<TimetableEntryResponse> entries = List.of(
                new TimetableEntryResponse(
                        1L, 1L, 2L, "Grade 10", 3L, "Section A", 1L, "2026-2027",
                        "MONDAY", 1L, 1, "Period 1", "08:00", "08:45", false,
                        10L, "Mathematics", "MATH-101", 5L, "Dr. Eleanor Rigby", "Room 304", "ACTIVE"
                )
        );

        StudentTimetableResponse resp = new StudentTimetableResponse(
                1L, "Siddharth Sharma", "SS-2024-0892", "14",
                1L, "Greenwood High", 2L, "Grade 10", 3L, "Section A",
                1L, "2026-2027", "MONDAY", LocalDate.of(2026, 9, 17),
                periods, entries
        );

        when(studentTimetableService.getStudentTimetable(any(), any(), any(), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/student/timetable")
                        .param("studentId", "1")
                        .param("schoolId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.studentName").value("Siddharth Sharma"))
                .andExpect(jsonPath("$.data.className").value("Grade 10"))
                .andExpect(jsonPath("$.data.sectionName").value("Section A"))
                .andExpect(jsonPath("$.data.periods[0].name").value("Period 1"))
                .andExpect(jsonPath("$.data.entries[0].subjectName").value("Mathematics"))
                .andExpect(jsonPath("$.data.entries[0].teacherName").value("Dr. Eleanor Rigby"));
    }
}
