package com.school.erp.controller.superadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.AddPerformanceReviewRequest;
import com.school.erp.dto.EmployeePerformanceDTO;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.superadmin.EmployeePerformanceService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = EmployeePerformanceController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, AuthFilterConfig.class})
)
class EmployeePerformanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeePerformanceService performanceService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testGetAllReviews() throws Exception {
        EmployeePerformanceDTO dto = new EmployeePerformanceDTO();
        dto.setId(1L);
        dto.setEmployeeId(1L);
        dto.setReviewCycle("Q3 2023");
        dto.setRating(5);

        when(performanceService.getAllReviews()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/super-admin/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reviewCycle").value("Q3 2023"))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void testAddReview() throws Exception {
        AddPerformanceReviewRequest request = new AddPerformanceReviewRequest();
        request.setEmployeeId(1L);
        request.setReviewCycle("Q3 2023");
        request.setRating(5);
        request.setComments("Great job!");

        EmployeePerformanceDTO responseDto = new EmployeePerformanceDTO();
        responseDto.setId(1L);
        responseDto.setEmployeeId(1L);
        responseDto.setReviewCycle("Q3 2023");
        responseDto.setRating(5);
        responseDto.setComments("Great job!");

        when(performanceService.addReview(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/super-admin/performance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reviewCycle").value("Q3 2023"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comments").value("Great job!"));
    }
}
