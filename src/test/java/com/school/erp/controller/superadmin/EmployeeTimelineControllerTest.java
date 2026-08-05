package com.school.erp.controller.superadmin;

import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.dto.EmployeeTimelineDTO;
import com.school.erp.service.superadmin.EmployeeTimelineService;
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
        controllers = EmployeeTimelineController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        JwtAuthenticationFilter.class,
                        AuthFilterConfig.class
                })
        }
)
public class EmployeeTimelineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeTimelineService timelineService;

    @Test
    public void testGetTimeline() throws Exception {
        EmployeeTimelineDTO dto = new EmployeeTimelineDTO();
        dto.setId(1L);
        dto.setTitle("Joined Company");

        when(timelineService.getTimelineByEmployeeId(anyLong())).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/v1/super-admin/employee-timeline/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Joined Company"));
    }
}
