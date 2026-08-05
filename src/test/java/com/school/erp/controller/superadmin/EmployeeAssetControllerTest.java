package com.school.erp.controller.superadmin;

import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.dto.EmployeeAssetDTO;
import com.school.erp.service.superadmin.EmployeeAssetService;
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
        controllers = EmployeeAssetController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JwtAuthenticationFilter.class,
                        AuthFilterConfig.class
                })
        }
)
public class EmployeeAssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeAssetService assetService;

    @Test
    public void testGetAssets() throws Exception {
        EmployeeAssetDTO dto = new EmployeeAssetDTO();
        dto.setId(1L);
        dto.setAssetName("MacBook Pro");

        when(assetService.getAssetsByEmployeeId(anyLong())).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/super-admin/employee-assets/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assetName").value("MacBook Pro"));
    }
}
