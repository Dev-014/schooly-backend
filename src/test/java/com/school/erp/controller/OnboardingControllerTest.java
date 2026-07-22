package com.school.erp.controller;

import com.school.erp.dto.onboarding.*;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.OnboardingDraftService;
import com.school.erp.service.OnboardingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockitoBean
    private OnboardingDraftService draftService;

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

    @Test
    void initDraft_shouldReturnOnboardingDraftDTO() throws Exception {
        OnboardingDraftDTO draft = new OnboardingDraftDTO(
                101L, "DRAFT", 1,
                Map.of("schoolName", "Greenwood Academy", "schoolCode", "GA-101"),
                Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                "2026-07-20T10:00:00"
        );
        when(draftService.initDraft(any(OnboardingInitRequest.class))).thenReturn(draft);

        mockMvc.perform(post("/api/v1/onboarding/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "schoolName": "Greenwood Academy",
                                  "schoolCode": "GA-101",
                                  "boardType": "IB",
                                  "principalEmail": "principal@greenwood.edu",
                                  "adminPhone": "9876543210"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.schoolId").value(101))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.step1.schoolName").value("Greenwood Academy"));
    }

    @Test
    void getDraft_shouldReturnDraftState() throws Exception {
        OnboardingDraftDTO draft = new OnboardingDraftDTO(
                101L, "DRAFT", 3,
                Map.of("schoolName", "Greenwood Academy"),
                Map.of(), Map.of("academicYear", "2026-2027"),
                Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                "2026-07-20T10:15:00"
        );
        when(draftService.getDraft(101L)).thenReturn(draft);

        mockMvc.perform(get("/api/v1/onboarding/draft/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.schoolId").value(101))
                .andExpect(jsonPath("$.data.currentStep").value(3));
    }

    @Test
    void saveStep_shouldReturnUpdatedDraft() throws Exception {
        OnboardingDraftDTO draft = new OnboardingDraftDTO(
                101L, "DRAFT", 5,
                Map.of("schoolName", "Greenwood Academy"),
                Map.of(), Map.of(), Map.of(),
                Map.of("studentsStatus", "COMPLETED", "staffStatus", "COMPLETED"),
                Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                "2026-07-20T10:30:00"
        );
        when(draftService.saveStep(any(OnboardingStepRequest.class))).thenReturn(draft);

        mockMvc.perform(post("/api/v1/onboarding/step")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "schoolId": 101,
                                  "stepNumber": 5,
                                  "payload": {
                                    "studentsStatus": "COMPLETED",
                                    "staffStatus": "COMPLETED"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.currentStep").value(5))
                .andExpect(jsonPath("$.data.step5.studentsStatus").value("COMPLETED"));
    }
}
