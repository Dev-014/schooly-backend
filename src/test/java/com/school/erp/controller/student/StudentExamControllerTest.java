package com.school.erp.controller.student;

import com.school.erp.dto.exam.StudentReportCardResponse;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentParent;
import com.school.erp.entity.student.StudentParentId;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.student.StudentParentRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthFilterConfig;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.JwtAuthenticationFilter;
import com.school.erp.service.exam.ExamAdmitCardService;
import com.school.erp.service.exam.ExamReportCardService;
import com.school.erp.service.exam.ExamScheduleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StudentExamController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class StudentExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthContextService authContextService;

    @MockitoBean
    private StudentRepository studentRepository;

    @MockitoBean
    private StudentParentRepository studentParentRepository;

    @MockitoBean
    private ExamScheduleService examScheduleService;

    @MockitoBean
    private ExamAdmitCardService examAdmitCardService;

    @MockitoBean
    private ExamReportCardService examReportCardService;

    private School mockSchool;
    private Student mockStudent;

    @BeforeEach
    void setUp() {
        mockSchool = new School();
        mockSchool.setId(1L);
        mockSchool.setName("Demo School");

        mockStudent = new Student();
        mockStudent.setId(101L);
        mockStudent.setSchool(mockSchool);
        mockStudent.setFirstName("Rahul");
        mockStudent.setLastName("Sharma");
    }

    @AfterEach
    void tearDown() {
        AuthContextHolder.clear();
    }

    @Test
    void student_GetResults_Success() throws Exception {
        AuthenticatedUser studentUser = new AuthenticatedUser(50L, 1L, UserRole.STUDENT);
        AuthContextHolder.set(studentUser);
        when(authContextService.requireCurrentUser()).thenReturn(studentUser);
        when(authContextService.resolveSchoolId(null)).thenReturn(1L);
        when(studentRepository.findByUserId(50L)).thenReturn(List.of(mockStudent));

        StudentReportCardResponse card = StudentReportCardResponse.builder()
                .id(1L)
                .studentId(101L)
                .studentName("Rahul Sharma")
                .rollNo("02")
                .admissionNo("AC-2026-001")
                .className("Grade 10")
                .sectionName("A")
                .termName("Term 1")
                .totalMarks(new BigDecimal("480.00"))
                .maxMarks(new BigDecimal("600.00"))
                .percentage(new BigDecimal("80.00"))
                .grade("A")
                .division("First")
                .status("PASS")
                .subjects(Collections.emptyList())
                .build();

        when(examReportCardService.getStudentReportCards(eq(1L), eq(101L), any())).thenReturn(List.of(card));

        mockMvc.perform(get("/api/v1/student/exams/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].studentName").value("Rahul Sharma"))
                .andExpect(jsonPath("$.data[0].grade").value("A"))
                .andExpect(jsonPath("$.data[0].status").value("PASS"));
    }

    @Test
    void parent_GetChildResults_Success() throws Exception {
        AuthenticatedUser parentUser = new AuthenticatedUser(200L, 1L, UserRole.PARENT);
        AuthContextHolder.set(parentUser);
        when(authContextService.requireCurrentUser()).thenReturn(parentUser);

        StudentParent sp = new StudentParent();
        sp.setId(new StudentParentId(200L, 101L));
        sp.setStudent(mockStudent);

        when(studentParentRepository.findByIdParentUserId(200L)).thenReturn(List.of(sp));

        StudentReportCardResponse card = StudentReportCardResponse.builder()
                .id(1L)
                .studentId(101L)
                .studentName("Rahul Sharma")
                .rollNo("02")
                .admissionNo("AC-2026-001")
                .className("Grade 10")
                .sectionName("A")
                .termName("Term 1")
                .totalMarks(new BigDecimal("480.00"))
                .maxMarks(new BigDecimal("600.00"))
                .percentage(new BigDecimal("80.00"))
                .grade("A")
                .division("First")
                .status("PASS")
                .subjects(Collections.emptyList())
                .build();

        when(examReportCardService.getStudentReportCards(eq(1L), eq(101L), any())).thenReturn(List.of(card));

        mockMvc.perform(get("/api/v1/student/exams/results").param("studentId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].studentName").value("Rahul Sharma"))
                .andExpect(jsonPath("$.data[0].percentage").value(80.00));
    }

    private com.school.erp.dto.exam.AdmitCardPreviewResponse previewWithStatus(String status) {
        return com.school.erp.dto.exam.AdmitCardPreviewResponse.builder()
                .candidateName("RAHUL SHARMA")
                .examName("ANNUAL EXAM")
                .termName("Term 1")
                .status(status)
                .cardNumber("AC-1-4-ADM")
                .schedule(List.of(com.school.erp.dto.exam.AdmitCardPreviewResponse.AdmitCardScheduleItem.builder()
                        .subjectName("Physics").build()))
                .build();
    }

    @Test
    void student_GetAdmitCard_WhenPending_HidesCardDetails() throws Exception {
        AuthenticatedUser studentUser = new AuthenticatedUser(50L, 1L, UserRole.STUDENT);
        AuthContextHolder.set(studentUser);
        when(authContextService.requireCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(50L)).thenReturn(List.of(mockStudent));
        when(examAdmitCardService.resolveDefaultExamSetupId(eq(1L), any())).thenReturn(4L);
        when(examAdmitCardService.getPreview(1L, 4L, 101L)).thenReturn(previewWithStatus("PENDING"));

        mockMvc.perform(get("/api/v1/student/exams/admit-card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.cardNumber").doesNotExist())
                .andExpect(jsonPath("$.data.schedule").isEmpty())
                .andExpect(jsonPath("$.message").value("Admit card is not generated yet for this examination"));
    }

    @Test
    void student_GetAdmitCard_WhenGenerated_ReturnsCard() throws Exception {
        AuthenticatedUser studentUser = new AuthenticatedUser(50L, 1L, UserRole.STUDENT);
        AuthContextHolder.set(studentUser);
        when(authContextService.requireCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(50L)).thenReturn(List.of(mockStudent));
        when(examAdmitCardService.getPreview(1L, 4L, 101L)).thenReturn(previewWithStatus("GENERATED"));

        mockMvc.perform(get("/api/v1/student/exams/admit-card").param("examSetupId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("GENERATED"))
                .andExpect(jsonPath("$.data.cardNumber").value("AC-1-4-ADM"))
                .andExpect(jsonPath("$.data.schedule[0].subjectName").value("Physics"));
    }

    @Test
    void parent_GetAdmitCard_WhenReleased_ReturnsCard() throws Exception {
        AuthenticatedUser parentUser = new AuthenticatedUser(200L, 1L, UserRole.PARENT);
        AuthContextHolder.set(parentUser);
        when(authContextService.requireCurrentUser()).thenReturn(parentUser);

        StudentParent sp = new StudentParent();
        sp.setId(new StudentParentId(200L, 101L));
        sp.setStudent(mockStudent);
        when(studentParentRepository.findByIdParentUserId(200L)).thenReturn(List.of(sp));
        when(examAdmitCardService.getPreview(1L, 4L, 101L)).thenReturn(previewWithStatus("RELEASED"));

        mockMvc.perform(get("/api/v1/student/exams/admit-card").param("examSetupId", "4").param("studentId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RELEASED"))
                .andExpect(jsonPath("$.data.cardNumber").value("AC-1-4-ADM"));
    }
}
