package com.school.erp.controller.superadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.superadmin.ModuleDto;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.superadmin.SuperAdminModuleService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = SuperAdminModuleController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class SuperAdminModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SuperAdminModuleService moduleService;

    @Test
    void getAllModules_ShouldReturnSuccess() throws Exception {
        ModuleDto dto = new ModuleDto(
                1L, "ADMIN_SETUP", "School Config", "Setup", true, "ACTIVE",
                "CORE", BigDecimal.ZERO, Arrays.asList("ADMIN"), Arrays.asList("Roles")
        );
        when(moduleService.getAllModules()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/super-admin/modules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].code").value("ADMIN_SETUP"))
                .andExpect(jsonPath("$.data[0].category").value("CORE"));
    }

    @Test
    void createModule_ShouldReturnCreatedModule() throws Exception {
        ModuleDto input = new ModuleDto(
                null, "FRONT_OFFICE", "Front Office", "Enquiry", true, "ACTIVE",
                "CORE", new BigDecimal("499.00"), Arrays.asList("ADMIN"), Arrays.asList("Enquiry")
        );
        ModuleDto output = new ModuleDto(
                2L, "FRONT_OFFICE", "Front Office", "Enquiry", true, "ACTIVE",
                "CORE", new BigDecimal("499.00"), Arrays.asList("ADMIN"), Arrays.asList("Enquiry")
        );
        when(moduleService.createModule(any(ModuleDto.class))).thenReturn(output);

        mockMvc.perform(post("/api/v1/super-admin/modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.code").value("FRONT_OFFICE"));
    }

    @Test
    void updateModule_ShouldReturnUpdatedModule() throws Exception {
        ModuleDto input = new ModuleDto(
                2L, "FRONT_OFFICE", "Updated Front Office", "New desc", true, "ACTIVE",
                "CORE", new BigDecimal("599.00"), Arrays.asList("ADMIN"), Arrays.asList("Enquiry", "Visitor Book")
        );
        when(moduleService.updateModule(eq(2L), any(ModuleDto.class))).thenReturn(input);

        mockMvc.perform(put("/api/v1/super-admin/modules/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("Updated Front Office"))
                .andExpect(jsonPath("$.data.addOnPrice").value(599.00));
    }
}
