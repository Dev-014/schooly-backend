package com.school.erp.controller.admin.frontoffice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.frontoffice.ComplaintRequest;
import com.school.erp.dto.frontoffice.ComplaintResponse;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.frontoffice.SchoolComplaintService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminComplaintController.class, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
})
class AdminComplaintControllerTest {

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper()
                        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        @MockitoBean
        private SchoolComplaintService complaintService;

        @MockitoBean
        private com.school.erp.service.auth.AuthorizationService authorizationService;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
                com.school.erp.security.AuthContextHolder.set(
                                new com.school.erp.security.AuthenticatedUser(100L, 1L,
                                                com.school.erp.entity.auth.UserRole.ADMIN));
                when(authorizationService.hasPermission(any(), any(), any())).thenReturn(true);
        }

        @org.junit.jupiter.api.AfterEach
        void tearDown() {
                com.school.erp.security.AuthContextHolder.clear();
        }

        @Test
        @DisplayName("GET /api/v1/admin/front-office/complaints returns complaints with complaintCode")
        void getComplaints_success() throws Exception {
                ComplaintResponse response = ComplaintResponse.builder()
                                .id(1L)
                                .complaintCode("CMP-00001")
                                .schoolId(1L)
                                .complainantName("Alice Teacher")
                                .complaintType("Facilities & Maintenance")
                                .description("Smart board defective")
                                .status("Open")
                                .sourceScope("OWN")
                                .priority("HIGH")
                                .isOwn(true)
                                .complaintDate(LocalDate.now())
                                .build();

                Page<ComplaintResponse> page = new PageImpl<>(List.of(response));
                when(complaintService.filterComplaints(any(), any(), any(), any(), any(), any(Pageable.class)))
                                .thenReturn(page);

                mockMvc.perform(get("/api/v1/admin/front-office/complaints")
                                .param("scope", "own")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data[0].complaintCode").value("CMP-00001"))
                                .andExpect(jsonPath("$.data[0].isOwn").value(true));
        }

        @Test
        @DisplayName("POST /api/v1/admin/front-office/complaints creates complaint")
        void createComplaint_success() throws Exception {
                ComplaintRequest request = ComplaintRequest.builder()
                                .complainantName("Bob Staff")
                                .complaintType("Student Grievance")
                                .description("Disruptive behavior in library")
                                .priority("NORMAL")
                                .build();

                ComplaintResponse response = ComplaintResponse.builder()
                                .id(2L)
                                .complaintCode("CMP-00002")
                                .schoolId(1L)
                                .complainantName("Bob Staff")
                                .complaintType("Student Grievance")
                                .status("Open")
                                .build();

                when(complaintService.createComplaint(any(), any(ComplaintRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/v1/admin/front-office/complaints")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.data.complaintCode").value("CMP-00002"));
        }
}
