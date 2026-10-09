package com.school.erp.controller.admin.homework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.school.erp.dto.homework.*;
import com.school.erp.entity.homework.AssignmentStatus;
import com.school.erp.entity.homework.AssignmentType;
import com.school.erp.entity.homework.SubmissionStatus;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.homework.HomeworkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminHomeworkController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class AdminHomeworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HomeworkService homeworkService;

    @MockitoBean
    private com.school.erp.service.auth.AuthorizationService authorizationService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        com.school.erp.security.AuthContextHolder.set(
                new com.school.erp.security.AuthenticatedUser(100L, 1L, com.school.erp.entity.auth.UserRole.ADMIN)
        );
        when(authorizationService.hasPermission(any(), any(), any())).thenReturn(true);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        com.school.erp.security.AuthContextHolder.clear();
    }

    @Test
    void getAssignments_ShouldReturnPageOfAssignments() throws Exception {
        HomeworkAssignmentResponse response = HomeworkAssignmentResponse.builder()
                .id(1L)
                .schoolId(1L)
                .assignmentType(AssignmentType.HOMEWORK)
                .classId(10L)
                .className("Grade 10")
                .sectionId(1L)
                .sectionName("Section A")
                .subjectId(2L)
                .subjectName("Mathematics")
                .title("Algebra Problem Set 1")
                .assignedDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(3))
                .status(AssignmentStatus.PUBLISHED)
                .build();

        when(homeworkService.filterAssignments(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/homework")
                        .param("type", "HOMEWORK")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Algebra Problem Set 1"));
    }

    @Test
    void getAssignmentById_ShouldReturnDetails() throws Exception {
        HomeworkAssignmentResponse response = HomeworkAssignmentResponse.builder()
                .id(1L)
                .title("Physics Lab Report")
                .assignmentType(AssignmentType.CLASSWORK)
                .assignedDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(2))
                .build();

        when(homeworkService.getAssignmentById(any(), eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/v1/homework/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.title").value("Physics Lab Report"));
    }

    @Test
    void createAssignment_ShouldReturnCreated() throws Exception {
        HomeworkAssignmentRequest request = HomeworkAssignmentRequest.builder()
                .assignmentType(AssignmentType.HOMEWORK)
                .classId(1L)
                .subjectId(1L)
                .title("Chemistry Worksheet 4")
                .assignedDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(2))
                .maxMarks(BigDecimal.valueOf(50))
                .build();

        HomeworkAssignmentResponse response = HomeworkAssignmentResponse.builder()
                .id(99L)
                .title("Chemistry Worksheet 4")
                .assignedDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(2))
                .build();

        when(homeworkService.createAssignment(any(), any(HomeworkAssignmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/homework")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(99));
    }

    @Test
    void getSubmissions_ShouldReturnList() throws Exception {
        HomeworkSubmissionResponse sub = HomeworkSubmissionResponse.builder()
                .id(10L)
                .assignmentId(1L)
                .studentId(5L)
                .studentName("Alice Smith")
                .status(SubmissionStatus.SUBMITTED)
                .submissionDate(LocalDateTime.now())
                .build();

        when(homeworkService.getSubmissionsForAssignment(any(), eq(1L))).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/v1/homework/1/submissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].studentName").value("Alice Smith"));
    }

    @Test
    void evaluateSubmission_ShouldReturnEvaluated() throws Exception {
        HomeworkEvaluationRequest evalReq = HomeworkEvaluationRequest.builder()
                .marksObtained(BigDecimal.valueOf(18))
                .remarks("Well done!")
                .status(SubmissionStatus.EVALUATED)
                .build();

        HomeworkSubmissionResponse evaluatedSub = HomeworkSubmissionResponse.builder()
                .id(10L)
                .marksObtained(BigDecimal.valueOf(18))
                .remarks("Well done!")
                .status(SubmissionStatus.EVALUATED)
                .build();

        when(homeworkService.evaluateSubmission(any(), eq(10L), any(HomeworkEvaluationRequest.class)))
                .thenReturn(evaluatedSub);

        mockMvc.perform(put("/api/v1/homework/submissions/10/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evalReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.marksObtained").value(18))
                .andExpect(jsonPath("$.data.remarks").value("Well done!"));
    }

    @Test
    void getUnassignedReport_ShouldReturnList() throws Exception {
        UnassignedReportItemResponse item = UnassignedReportItemResponse.builder()
                .id(1L)
                .className("Grade 10")
                .subjectName("Mathematics")
                .faculty("Unassigned")
                .status("Urgent")
                .build();

        when(homeworkService.getUnassignedReport(any(), any(), any(), any())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/homework/reports/unassigned")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].status").value("Urgent"));
    }

    @Test
    void createAssignment_WithAliases_ShouldReturnCreated() throws Exception {
        String jsonPayload = """
                {
                    "classId": 1,
                    "sectionId": 2,
                    "subjectId": 3,
                    "title": "Algebra Worksheet",
                    "assignmentDate": "2026-10-10",
                    "dueDate": "2026-10-14",
                    "totalMarks": 100
                }
                """;

        HomeworkAssignmentResponse response = HomeworkAssignmentResponse.builder()
                .id(105L)
                .title("Algebra Worksheet")
                .assignedDate(LocalDate.of(2026, 10, 10))
                .dueDate(LocalDate.of(2026, 10, 14))
                .maxMarks(BigDecimal.valueOf(100))
                .build();

        when(homeworkService.createAssignment(any(), any(HomeworkAssignmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/homework")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.assignmentDate").value("2026-10-10"))
                .andExpect(jsonPath("$.data.totalMarks").value(100));
    }

    @Test
    void evaluateSubmission_WithGradedStatus_ShouldReturnOk() throws Exception {
        String jsonPayload = """
                {
                    "marksObtained": 45,
                    "remarks": "Great work",
                    "status": "GRADED"
                }
                """;

        HomeworkSubmissionResponse evaluatedSub = HomeworkSubmissionResponse.builder()
                .id(10L)
                .marksObtained(BigDecimal.valueOf(45))
                .remarks("Great work")
                .status(SubmissionStatus.GRADED)
                .build();

        when(homeworkService.evaluateSubmission(any(), eq(10L), any(HomeworkEvaluationRequest.class)))
                .thenReturn(evaluatedSub);

        mockMvc.perform(put("/api/v1/homework/submissions/10/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.status").value("GRADED"))
                .andExpect(jsonPath("$.data.submissionStatus").value("GRADED"));
    }
}
