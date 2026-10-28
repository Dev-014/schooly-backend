package com.school.erp.controller.student;

import com.school.erp.dto.student.StudentLeaveRequest;
import com.school.erp.dto.student.StudentLeaveResponse;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.student.StudentLeaveService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StudentLeaveController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class StudentLeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentLeaveService leaveService;

    @MockitoBean
    private AuthContextService authContextService;

    @MockitoBean
    private AuthorizationService authorizationService;

    @AfterEach
    void tearDown() {
        com.school.erp.security.AuthContextHolder.clear();
    }

    @Test
    void getAllLeaves_withoutStudentId_returnsAllSchoolLeaves() throws Exception {
        StudentLeaveResponse leave1 = new StudentLeaveResponse(
                1L, 1L, 10L, "Rahul Sharma", "10045", "Standard 7th",
                LocalDate.now(), LocalDate.now(), LocalDate.now().plusDays(2), 3,
                "Medical fever", "PENDING", null, LocalDate.now().atStartOfDay()
        );
        StudentLeaveResponse leave2 = new StudentLeaveResponse(
                2L, 1L, 20L, "Priya Sharma", "10048", "Standard 5th",
                LocalDate.now(), LocalDate.now(), LocalDate.now().plusDays(1), 2,
                "Family trip", "APPROVED", "Approved by class teacher", LocalDate.now().atStartOfDay()
        );

        when(leaveService.getAllLeaves(eq(1L), eq(null))).thenReturn(List.of(leave1, leave2));

        mockMvc.perform(get("/api/student-leaves")
                        .param("schoolId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].studentName").value("Rahul Sharma"))
                .andExpect(jsonPath("$.data[1].studentName").value("Priya Sharma"));
    }

    @Test
    void getAllLeaves_withStudentId_returnsTargetStudentLeaves() throws Exception {
        StudentLeaveResponse leave = new StudentLeaveResponse(
                2L, 1L, 20L, "Priya Sharma", "10048", "Standard 5th",
                LocalDate.now(), LocalDate.now(), LocalDate.now().plusDays(1), 2,
                "Family trip", "APPROVED", "Approved by class teacher", LocalDate.now().atStartOfDay()
        );

        when(leaveService.getAllLeaves(eq(1L), eq(20L))).thenReturn(List.of(leave));

        mockMvc.perform(get("/api/student-leaves")
                        .param("schoolId", "1")
                        .param("studentId", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentId").value(20))
                .andExpect(jsonPath("$.data[0].studentName").value("Priya Sharma"));
    }

    @Test
    void createLeave_validRequest_createsSuccessfully() throws Exception {
        StudentLeaveResponse created = new StudentLeaveResponse(
                10L, 1L, 15L, "Paras Patel", "AC-2024-001", "Grade 10",
                LocalDate.now(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 3,
                "Sister wedding", "PENDING", null, LocalDate.now().atStartOfDay()
        );

        when(leaveService.createLeave(any(StudentLeaveRequest.class))).thenReturn(created);

        String jsonBody = """
                {
                    "schoolId": 1,
                    "studentId": 15,
                    "fromDate": "2026-10-15",
                    "toDate": "2026-10-17",
                    "days": 3,
                    "reason": "Sister wedding"
                }
                """;

        mockMvc.perform(post("/api/student-leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.studentName").value("Paras Patel"));
    }

    @Test
    void updateLeave_withPermission_updatesSuccessfully() throws Exception {
        com.school.erp.security.AuthContextHolder.set(
                new com.school.erp.security.AuthenticatedUser(1L, 1L, com.school.erp.entity.auth.UserRole.ADMIN));
        when(authorizationService.hasPermission(eq(1L), eq(1L), eq("leave_management.student_leaves.edit"))).thenReturn(true);

        StudentLeaveResponse updated = new StudentLeaveResponse(
                10L, 1L, 15L, "Paras Patel", "AC-2024-001", "Grade 10",
                LocalDate.now(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 3,
                "Sister wedding", "APPROVED", "Approved by Principal", LocalDate.now().atStartOfDay()
        );

        when(leaveService.updateLeave(eq(10L), any(StudentLeaveRequest.class))).thenReturn(updated);

        String jsonBody = """
                {
                    "schoolId": 1,
                    "studentId": 15,
                    "fromDate": "2026-10-15",
                    "toDate": "2026-10-17",
                    "days": 3,
                    "reason": "Sister wedding",
                    "status": "APPROVED",
                    "reply": "Approved by Principal"
                }
                """;

        mockMvc.perform(put("/api/student-leaves/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.reply").value("Approved by Principal"));
    }

    @Test
    void updateLeave_withoutPermission_isForbidden() throws Exception {
        com.school.erp.security.AuthContextHolder.set(
                new com.school.erp.security.AuthenticatedUser(15L, 1L, com.school.erp.entity.auth.UserRole.STUDENT));
        when(authorizationService.hasPermission(eq(1L), eq(15L), eq("leave_management.student_leaves.edit"))).thenReturn(false);

        String jsonBody = """
                {
                    "schoolId": 1,
                    "studentId": 15,
                    "fromDate": "2026-10-15",
                    "toDate": "2026-10-17",
                    "days": 3,
                    "reason": "Sister wedding",
                    "status": "APPROVED"
                }
                """;

        mockMvc.perform(put("/api/student-leaves/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
