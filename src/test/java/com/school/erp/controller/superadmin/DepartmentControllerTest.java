package com.school.erp.controller.superadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.CreateDepartmentRequest;
import com.school.erp.dto.DepartmentDTO;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.superadmin.DepartmentService;
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
    controllers = DepartmentController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, AuthFilterConfig.class})
)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testGetAllDepartments() throws Exception {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(1L);
        dto.setName("HR");

        when(departmentService.getAllDepartments()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/super-admin/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("HR"));
    }

    @Test
    void testCreateDepartment() throws Exception {
        CreateDepartmentRequest request = new CreateDepartmentRequest();
        request.setName("Engineering");

        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(2L);
        dto.setName("Engineering");

        when(departmentService.createDepartment(any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/super-admin/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Engineering"));
    }
}
