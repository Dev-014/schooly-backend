package com.school.erp.controller.superadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.EmployeePayrollDTO;
import com.school.erp.dto.RunPayrollRequest;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.superadmin.EmployeePayrollService;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = EmployeePayrollController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, AuthFilterConfig.class})
)
class EmployeePayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeePayrollService payrollService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testGetAllPayrolls() throws Exception {
        EmployeePayrollDTO dto = new EmployeePayrollDTO();
        dto.setId(1L);
        dto.setEmployeeId(1L);
        dto.setMonth("October");
        dto.setYear(2023);
        dto.setBaseSalary(new BigDecimal("5000.00"));

        when(payrollService.getAllPayrolls()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/super-admin/payroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].month").value("October"))
                .andExpect(jsonPath("$[0].baseSalary").value(5000.00));
    }

    @Test
    void testRunPayroll() throws Exception {
        RunPayrollRequest request = new RunPayrollRequest();
        request.setEmployeeId(1L);
        request.setMonth("October");
        request.setYear(2023);
        request.setBaseSalary(new BigDecimal("5000.00"));
        request.setPaymentDate(LocalDate.of(2023, 10, 31));

        EmployeePayrollDTO responseDto = new EmployeePayrollDTO();
        responseDto.setId(1L);
        responseDto.setEmployeeId(1L);
        responseDto.setMonth("October");
        responseDto.setYear(2023);
        responseDto.setBaseSalary(new BigDecimal("5000.00"));

        when(payrollService.runPayroll(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/super-admin/payroll/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.month").value("October"))
                .andExpect(jsonPath("$.baseSalary").value(5000.00));
    }
}
