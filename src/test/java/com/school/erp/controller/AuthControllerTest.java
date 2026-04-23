package com.school.erp.controller;

import com.school.erp.dto.auth.AuthTokenResponse;
import com.school.erp.dto.auth.AuthUserResponse;
import com.school.erp.dto.auth.UserSchoolResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
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
