package com.school.erp.controller;

import com.school.erp.dto.auth.AuthTokenResponse;
import com.school.erp.dto.auth.AuthUserResponse;
import com.school.erp.dto.auth.UserSchoolResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.dto.auth.LoginVerifyResponse;
import com.school.erp.dto.auth.OtpVerifyRequest;
import com.school.erp.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_shouldReturnUserPayload() throws Exception {
        when(authService.loginOrSignup("9999999999"))
                .thenReturn(new AuthUserResponse(1L, "9999999999", null, null, "ACTIVE", true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "9999999999"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.phone").value("9999999999"))
                .andExpect(jsonPath("$.data.newlyCreated").value(true));
    }

    @Test
    void getUserSchools_shouldReturnMappedSchools() throws Exception {
        when(authService.getUserSchools(1L))
                .thenReturn(List.of(new UserSchoolResponse(10L, "Alpha School", "ALPHA", "TEACHER", "ACTIVE")));

        mockMvc.perform(get("/auth/schools").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].schoolId").value(10))
                .andExpect(jsonPath("$.data[0].role").value("TEACHER"));
    }

    @Test
    void verifyOtp_shouldReturnVerifyPayloadWithFlags() throws Exception {
        LoginVerifyResponse response = new LoginVerifyResponse(
                1L, "9999999999", "John Admin", "admin@example.com",
                "SUPER_ADMIN", List.of("SUPER_ADMIN"), List.of(),
                false, false, List.of(),
                "access-token", "refresh-token", List.of("ALL")
        );
        when(authService.verifyOtp(any(OtpVerifyRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "9999999999",
                                  "otp": "1111"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.primaryRole").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$.data.requiresSchoolSelection").value(false))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void logout_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void selectSchool_shouldReturnTokenPayload() throws Exception {
        when(authService.selectSchool(eq(1L), eq(10L), isNull()))
                .thenReturn(new AuthTokenResponse(1L, 10L, "TEACHER", "access-token", "refresh-token"));

        mockMvc.perform(post("/auth/select-school")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "schoolId": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.schoolId").value(10))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }
}
