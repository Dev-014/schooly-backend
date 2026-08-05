package com.school.erp.controller.superadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.CreateSuperAdminEmployeeRequest;
import com.school.erp.dto.SuperAdminEmployeeDTO;
import com.school.erp.service.superadmin.SuperAdminEmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;

@WebMvcTest(
    controllers = SuperAdminEmployeeController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, AuthFilterConfig.class})
)
public class SuperAdminEmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SuperAdminEmployeeService employeeService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGetAllEmployees() throws Exception {
        SuperAdminEmployeeDTO dto = new SuperAdminEmployeeDTO();
        dto.setId(1L);
        dto.setName("Test Employee");

        when(employeeService.getAllEmployees()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/super-admin/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Employee"));
    }

    @Test
    void shouldGetEmployeeById() throws Exception {
        SuperAdminEmployeeDTO dto = new SuperAdminEmployeeDTO();
        dto.setId(1L);
        dto.setName("Test Employee");

        when(employeeService.getEmployeeById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/super-admin/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Employee"));
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        CreateSuperAdminEmployeeRequest request = new CreateSuperAdminEmployeeRequest();
        request.setName("New Employee");
        request.setEmail("test@test.com");
        request.setPhone("1234567890");
        request.setPassword("password123");

        SuperAdminEmployeeDTO dto = new SuperAdminEmployeeDTO();
        dto.setId(2L);
        dto.setName("New Employee");

        when(employeeService.createEmployee(any(CreateSuperAdminEmployeeRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/super-admin/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Employee"));
    }
}
