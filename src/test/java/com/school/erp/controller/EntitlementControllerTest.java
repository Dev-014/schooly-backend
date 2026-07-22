package com.school.erp.controller;

import com.school.erp.dto.catalog.EntitlementEvaluationDto;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.EntitlementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EntitlementController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class EntitlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EntitlementService entitlementService;

    @Test
    void shouldEvaluateEntitlementsSuccessfully() throws Exception {
        EntitlementEvaluationDto dto = new EntitlementEvaluationDto(
                101L, "PREMIUM", "Premium Partner",
                new HashSet<>(Collections.singletonList("ATTENDANCE")),
                Collections.singletonMap("BUS_TRACKING", "TRIAL"),
                true, LocalDateTime.now()
        );
        when(entitlementService.evaluateEntitlements(101L)).thenReturn(dto);

        mockMvc.perform(get("/entitlements/school/101").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.activePlanCode").value("PREMIUM"))
                .andExpect(jsonPath("$.data.trialActive").value(true));
    }
}
