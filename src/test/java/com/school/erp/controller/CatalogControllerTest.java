package com.school.erp.controller;

import com.school.erp.dto.catalog.*;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.CatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CatalogController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogService catalogService;

    @Test
    void shouldReturnActivePlans() throws Exception {
        CatalogPlanDto plan = new CatalogPlanDto(
                1L, "GROWTH", "Pro Growth", new BigDecimal("1499.00"), new BigDecimal("14999.00"),
                500, 50, "Pro description", Arrays.asList("Feature 1", "Feature 2"),
                Arrays.asList("ATTENDANCE", "EXAMS", "FINANCE"), "ACTIVE"
        );
        when(catalogService.getActivePlans()).thenReturn(Collections.singletonList(plan));

        mockMvc.perform(get("/catalog/plans").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].code").value("GROWTH"))
                .andExpect(jsonPath("$.data[0].bundledModuleCodes[0]").value("ATTENDANCE"));
    }

    @Test
    void shouldReturnActiveModules() throws Exception {
        CatalogModuleDto module = new CatalogModuleDto(
                1L, "ATTENDANCE", "Attendance Management", "Daily tracking", "CORE", BigDecimal.ZERO, true, "ACTIVE"
        );
        when(catalogService.getActiveModules()).thenReturn(Collections.singletonList(module));

        mockMvc.perform(get("/catalog/modules").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].code").value("ATTENDANCE"))
                .andExpect(jsonPath("$.data[0].category").value("CORE"));
    }
}
