package com.school.erp.controller.superadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.EmployeeAuditLogDTO;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.superadmin.EmployeeAuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = EmployeeAuditLogController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, AuthFilterConfig.class})
)
class EmployeeAuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeAuditLogService auditLogService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetAllAuditLogs() throws Exception {
        EmployeeAuditLogDTO dto = new EmployeeAuditLogDTO();
        dto.setId(1L);
        dto.setAction("CREATE_EMPLOYEE");
        dto.setEntityType("SUPER_ADMIN_EMPLOYEE");
        dto.setEntityId(1L);

        when(auditLogService.getAllAuditLogs()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/super-admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].action").value("CREATE_EMPLOYEE"));
    }
}
