package com.school.erp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.student.StudentProfileResponse;
import com.school.erp.dto.student.UpdateStudentProfileRequest;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.StudentProfileService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StudentProfileController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class StudentProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private StudentProfileService studentProfileService;

    @Test
    void getProfile_ShouldReturnStudentProfileResponse() throws Exception {
        StudentProfileResponse resp = new StudentProfileResponse(
                1L, 101L, "Siddharth Sharma", "SS-2024-0892", "14", "ACTIVE",
                LocalDate.of(2021, 8, 15), 1L, "Greenwood High", 2L, "Grade 12", 3L, "Science (A)",
                1L, "Siddharth", "Sharma", "Male", LocalDate.of(2007, 5, 22),
                "B+", "Hinduism", "General", "Indian", "Delhi Public School",
                "B-402, High-End Apartments, Sector 15, Dwarka, New Delhi", "Same as current address",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e",
                "+91 98765 43210", "siddharth.s@school.edu",
                "Mr. Rajesh Sharma", "Father", "+91 98765 43210", "rajesh.sharma@example.com", "Business",
                "Mr. Rajesh Sharma", "+91 98765 43210", "rajesh.sharma@example.com", "Business",
                "Mrs. Sunita Sharma", "+91 98765 43211", "sunita.sharma@example.com", "Homemaker",
                1L, "General", 1L, "Indigo House (Blue)", null,
                "State Bank of India", "XXXX XXXX 8902", "SBIN0001234",
                "Top 5% in Academic Excellence",
                BigDecimal.valueOf(94.8), BigDecimal.valueOf(12400.00), 3
        );

        when(studentProfileService.getProfile(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(get("/api/v1/student/profile")
                        .param("studentId", "1")
                        .param("schoolId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("Siddharth Sharma"))
                .andExpect(jsonPath("$.data.admissionNo").value("SS-2024-0892"))
                .andExpect(jsonPath("$.data.rollNumber").value("14"))
                .andExpect(jsonPath("$.data.outstandingFees").value(12400.0))
                .andExpect(jsonPath("$.data.attendancePercentage").value(94.8));
    }

    @Test
    void updateProfile_ShouldReturnUpdatedProfile() throws Exception {
        UpdateStudentProfileRequest req = new UpdateStudentProfileRequest(
                "Siddharth", "Sharma", "+91 98765 43210", "siddharth.new@school.edu",
                "Male", LocalDate.of(2007, 5, 22), "B+", "Hinduism", "General",
                "New Address Line 1", "Same as current address", null,
                "Mr. Rajesh Sharma", "Father", "+91 98765 43210", "rajesh@example.com", "Business",
                "Mr. Rajesh Sharma", "+91 98765 43210", "rajesh@example.com", "Business",
                "Mrs. Sunita Sharma", "+91 98765 43211", "sunita@example.com", "Homemaker",
                "State Bank of India", "XXXX XXXX 8902", "SBIN0001234"
        );

        StudentProfileResponse updated = new StudentProfileResponse(
                1L, 101L, "Siddharth Sharma", "SS-2024-0892", "14", "ACTIVE",
                LocalDate.of(2021, 8, 15), 1L, "Greenwood High", 2L, "Grade 12", 3L, "Science (A)",
                1L, "Siddharth", "Sharma", "Male", LocalDate.of(2007, 5, 22),
                "B+", "Hinduism", "General", "Indian", "Delhi Public School",
                "New Address Line 1", "Same as current address", null,
                "+91 98765 43210", "siddharth.new@school.edu",
                "Mr. Rajesh Sharma", "Father", "+91 98765 43210", "rajesh@example.com", "Business",
                "Mr. Rajesh Sharma", "+91 98765 43210", "rajesh@example.com", "Business",
                "Mrs. Sunita Sharma", "+91 98765 43211", "sunita@example.com", "Homemaker",
                1L, "General", 1L, "Indigo House (Blue)", null,
                "State Bank of India", "XXXX XXXX 8902", "SBIN0001234",
                "Top 5% in Academic Excellence",
                BigDecimal.valueOf(94.8), BigDecimal.valueOf(12400.00), 3
        );

        when(studentProfileService.updateProfile(any(), any(), any(), any(UpdateStudentProfileRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/student/profile")
                        .param("studentId", "1")
                        .param("schoolId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.email").value("siddharth.new@school.edu"))
                .andExpect(jsonPath("$.data.address").value("New Address Line 1"));
    }
}
