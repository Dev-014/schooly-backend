package com.school.erp.controller;

import com.school.erp.dto.onboarding.OnboardingRegisterRequest;
import com.school.erp.dto.onboarding.OnboardingRegisterResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.OnboardingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OnboardingController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OnboardingService onboardingService;

    @Test
    void register_shouldReturnOnboardingResponse() throws Exception {
        OnboardingRegisterResponse response = new OnboardingRegisterResponse(
                100L, "New Age School", "NAS100", 50L, "9111111111", "ACTIVE",
                Map.of("themeColor", "blue", "enableAi", true)
        );
        when(onboardingService.registerSchool(any(OnboardingRegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/onboarding/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "schoolName": "New Age School",
                                  "schoolCode": "NAS100",
                                  "adminName": "Principal Smith",
                                  "adminPhone": "9111111111",
                                  "metadata": {
                                    "themeColor": "blue",
                                    "enableAi": true
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.schoolId").value(100))
                .andExpect(jsonPath("$.data.schoolCode").value("NAS100"))
                .andExpect(jsonPath("$.data.metadata.themeColor").value("blue"))
                .andExpect(jsonPath("$.data.metadata.enableAi").value(true));
    }
}
