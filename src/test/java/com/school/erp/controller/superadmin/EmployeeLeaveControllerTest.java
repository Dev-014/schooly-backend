package com.school.erp.controller.superadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.ApplyLeaveRequest;
import com.school.erp.dto.EmployeeLeaveDTO;
import com.school.erp.service.superadmin.EmployeeLeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
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
    controllers = EmployeeLeaveController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, AuthFilterConfig.class})
)
class EmployeeLeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeLeaveService leaveService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testGetAllLeaves() throws Exception {
        EmployeeLeaveDTO dto = new EmployeeLeaveDTO();
        dto.setId(1L);
        dto.setEmployeeId(1L);
        dto.setLeaveType("SICK");
        dto.setStatus("PENDING");

        when(leaveService.getAllLeaves()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/super-admin/leaves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].leaveType").value("SICK"));
    }

    @Test
    void testApplyLeave() throws Exception {
        ApplyLeaveRequest request = new ApplyLeaveRequest();
        request.setEmployeeId(1L);
        request.setLeaveType("SICK");
        request.setStartDate(LocalDate.of(2023, 10, 24));
        request.setEndDate(LocalDate.of(2023, 10, 25));

        EmployeeLeaveDTO responseDto = new EmployeeLeaveDTO();
        responseDto.setId(1L);
        responseDto.setEmployeeId(1L);
        responseDto.setLeaveType("SICK");
        responseDto.setStatus("PENDING");

        when(leaveService.applyLeave(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/super-admin/leaves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.leaveType").value("SICK"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
