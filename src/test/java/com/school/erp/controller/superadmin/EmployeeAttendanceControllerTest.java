package com.school.erp.controller.superadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.EmployeeAttendanceDTO;
import com.school.erp.dto.MarkAttendanceRequest;
import com.school.erp.service.superadmin.EmployeeAttendanceService;
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
    controllers = EmployeeAttendanceController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, AuthFilterConfig.class})
)
class EmployeeAttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeAttendanceService attendanceService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testGetAttendanceByDate() throws Exception {
        EmployeeAttendanceDTO dto = new EmployeeAttendanceDTO();
        dto.setId(1L);
        dto.setEmployeeId(1L);
        dto.setDate(LocalDate.of(2023, 10, 24));
        dto.setStatus("PRESENT");

        when(attendanceService.getAttendanceByDate(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/super-admin/attendance")
                .param("date", "2023-10-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PRESENT"));
    }

    @Test
    void testMarkAttendance() throws Exception {
        MarkAttendanceRequest request = new MarkAttendanceRequest();
        request.setEmployeeId(1L);
        request.setDate(LocalDate.of(2023, 10, 24));
        request.setStatus("PRESENT");

        EmployeeAttendanceDTO responseDto = new EmployeeAttendanceDTO();
        responseDto.setId(1L);
        responseDto.setEmployeeId(1L);
        responseDto.setDate(LocalDate.of(2023, 10, 24));
        responseDto.setStatus("PRESENT");

        when(attendanceService.markAttendance(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/super-admin/attendance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PRESENT"));
    }
}
