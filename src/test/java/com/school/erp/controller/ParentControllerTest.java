package com.school.erp.controller;

import com.school.erp.dto.parent.ParentChildResponse;
import com.school.erp.entity.UserRole;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.ParentService;
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
        controllers = ParentController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class ParentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParentService parentService;

    @MockitoBean
    private AuthContextService authContextService;

    @Test
    void getChildren_shouldReturnChildrenForAuthenticatedParent() throws Exception {
        when(authContextService.requireCurrentUser())
                .thenReturn(new AuthenticatedUser(7L, 10L, UserRole.PARENT));
        when(parentService.getChildren(7L, 10L))
                .thenReturn(List.of(new ParentChildResponse(21L, "Alex", "ADM-21", 10L, 3L)));

        mockMvc.perform(get("/parent/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].studentId").value(21))
                .andExpect(jsonPath("$.data[0].name").value("Alex"))
                .andExpect(jsonPath("$.data[0].schoolId").value(10));
    }
}
