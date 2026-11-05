package com.school.erp.controller.academic;

import com.school.erp.dto.academic.SubjectDirectoryItemResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.academic.AcademicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AcademicController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class AcademicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AcademicService academicService;

    @Test
    void getSubjectDirectory_ShouldReturnSubjectsList() throws Exception {
        List<SubjectDirectoryItemResponse> mockDirectory = List.of(
                new SubjectDirectoryItemResponse(
                        10L, "Mathematics", "MAT-001", "THEORY", 4, "Grade 10",
                        1L, "10", 1L, "A", "2025-2026",
                        5L, "Prakash Kala", "prakash@school.edu", "HOD", "Mathematics", "ACTIVE"
                ),
                new SubjectDirectoryItemResponse(
                        11L, "Physics", "PHY-001", "THEORY", 4, "Grade 10",
                        1L, "10", 1L, "A", "2025-2026",
                        6L, "Ritu Maltare", "ritu@school.edu", "Senior Teacher", "Science", "ACTIVE"
                )
        );

        when(academicService.getSubjectDirectory(any(), any(), any())).thenReturn(mockDirectory);

        mockMvc.perform(get("/api/v1/academics/subject-directory")
                        .param("schoolId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].subjectName").value("Mathematics"))
                .andExpect(jsonPath("$.data[0].subjectCode").value("MAT-001"))
                .andExpect(jsonPath("$.data[0].teacherName").value("Prakash Kala"))
                .andExpect(jsonPath("$.data[1].subjectName").value("Physics"));
    }
}

