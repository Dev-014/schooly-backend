package com.school.erp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.academic.BulkTimetableRequest;
import com.school.erp.dto.academic.TimetableEntryRequest;
import com.school.erp.dto.academic.TimetableEntryResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.AcademicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AcademicController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class AcademicControllerTimetableTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AcademicService academicService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getTimetableGrid_ShouldReturnList() throws Exception {
        TimetableEntryResponse response = new TimetableEntryResponse(
                1L, 1L, 1L, "Class 1", null, null, 1L, "2025-2026",
                "MONDAY", 1L, 1, "Period 1", "08:30", "09:15", false,
                1L, "Math", "MATH101", 1L, "John Doe", "101", "ACTIVE"
        );

        when(academicService.getTimetableGrid(1L, 1L, null, "MONDAY", 1L, null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/academics/timetable-entries")
                        .param("schoolId", "1")
                        .param("classId", "1")
                        .param("dayOfWeek", "MONDAY")
                        .param("academicYearId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    void saveTimetableGrid_ShouldReturnCreated() throws Exception {
        TimetableEntryRequest entryReq = new TimetableEntryRequest(
                1L, 1L, null, 1L, "MONDAY", 1L, 1L, 1L, "101"
        );
        BulkTimetableRequest request = new BulkTimetableRequest(1L, 1L, null, 1L, List.of(entryReq));

        TimetableEntryResponse response = new TimetableEntryResponse(
                1L, 1L, 1L, "Class 1", null, null, 1L, "2025-2026",
                "MONDAY", 1L, 1, "Period 1", "08:30", "09:15", false,
                1L, "Math", "MATH101", 1L, "John Doe", "101", "ACTIVE"
        );

        when(academicService.saveTimetableGrid(any(BulkTimetableRequest.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(post("/api/v1/academics/timetable-entries/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    @Test
    void deleteTimetableEntry_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/academics/timetable-entries/1")
                        .param("schoolId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
