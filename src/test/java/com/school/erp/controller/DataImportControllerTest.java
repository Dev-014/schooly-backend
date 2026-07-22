package com.school.erp.controller;

import com.school.erp.dto.importing.DataImportErrorDTO;
import com.school.erp.dto.importing.DataImportJobDTO;
import com.school.erp.dto.importing.DataImportMappingDTO;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.DataImportService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = DataImportController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class DataImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataImportService dataImportService;

    @Test
    void listJobs_shouldReturnJobsList() throws Exception {
        DataImportJobDTO job = new DataImportJobDTO(
                1L, 101L, "students", "students.csv", "IN_PROGRESS",
                120, 117, 3,
                List.of(new DataImportMappingDTO(1L, "Adm_Number", "AC-100", "Admission Number", 99, "normal")),
                List.of(new DataImportErrorDTO(10L, "Row 14", "Demographics", "Gender", "Missing gender", "", false)),
                "2026-07-20T10:00:00"
        );
        when(dataImportService.listJobs(101L, "students")).thenReturn(List.of(job));

        mockMvc.perform(get("/api/v1/import/jobs")
                        .param("schoolId", "101")
                        .param("category", "students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].jobId").value(1))
                .andExpect(jsonPath("$.data[0].category").value("students"))
                .andExpect(jsonPath("$.data[0].totalRecords").value(120));
    }

    @Test
    void resolveError_shouldUpdateErrorAndReturnDTO() throws Exception {
        DataImportErrorDTO resolvedError = new DataImportErrorDTO(
                10L, "Row 14", "Demographics", "Gender", "Missing gender", "MALE", true
        );
        when(dataImportService.resolveError(eq(10L), eq("MALE"))).thenReturn(resolvedError);

        mockMvc.perform(post("/api/v1/import/errors/10/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newValue": "MALE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.errorId").value(10))
                .andExpect(jsonPath("$.data.currentValue").value("MALE"))
                .andExpect(jsonPath("$.data.resolved").value(true));
    }

    @Test
    void commitJob_shouldFinalizeJobAndReturnDTO() throws Exception {
        DataImportJobDTO completedJob = new DataImportJobDTO(
                1L, 101L, "students", "students.csv", "COMPLETED",
                120, 120, 0, List.of(), List.of(), "2026-07-20T10:00:00"
        );
        when(dataImportService.commitJob(1L)).thenReturn(completedJob);

        mockMvc.perform(post("/api/v1/import/job/1/commit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}
