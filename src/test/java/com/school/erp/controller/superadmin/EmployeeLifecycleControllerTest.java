package com.school.erp.controller.superadmin;

import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.dto.EmployeeLifecycleDTO;
import com.school.erp.service.superadmin.EmployeeLifecycleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EmployeeLifecycleController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JwtAuthenticationFilter.class,
                        AuthFilterConfig.class
                })
        }
)
public class EmployeeLifecycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeLifecycleService lifecycleService;

    @Test
    public void testGetLifecycle() throws Exception {
        EmployeeLifecycleDTO dto = new EmployeeLifecycleDTO();
        dto.setId(1L);
        dto.setEventType("PROMOTION");

        when(lifecycleService.getLifecycleEventsByEmployeeId(anyLong())).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/super-admin/employee-lifecycle/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("PROMOTION"));
    }
}
