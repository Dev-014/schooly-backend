package com.school.erp.controller.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.homework.HomeworkAssignmentResponse;
import com.school.erp.dto.homework.HomeworkSubmissionRequest;
import com.school.erp.dto.homework.HomeworkSubmissionResponse;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.homework.AssignmentStatus;
import com.school.erp.entity.homework.AssignmentType;
import com.school.erp.entity.homework.SubmissionStatus;
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
import com.school.erp.service.homework.HomeworkService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StudentHomeworkController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthFilterConfig.class)
        }
)
class StudentHomeworkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private AuthContextService authContextService;

    @MockitoBean
    private HomeworkService homeworkService;

    @MockitoBean
    private StudentRepository studentRepository;

    @MockitoBean
    private StudentParentRepository studentParentRepository;

    private School mockSchool;
    private Student mockStudent;
    private HomeworkAssignmentResponse mockAssignment;

    @BeforeEach
    void setUp() {
        mockSchool = new School();
        mockSchool.setId(1000L);
        mockSchool.setName("Demo Academy");

        mockStudent = new Student();
        mockStudent.setId(17L);
        mockStudent.setSchool(mockSchool);
        mockStudent.setFirstName("Rahul");
        mockStudent.setLastName("Patel");

        mockAssignment = HomeworkAssignmentResponse.builder()
                .id(1L)
                .schoolId(1000L)
                .assignmentType(AssignmentType.HOMEWORK)
                .classId(2L)
                .className("Grade 10")
                .sectionId(5L)
                .sectionName("Section A")
                .subjectId(7L)
                .subjectName("Mathematics")
                .subjectCode("MAT-101")
                .title("Trigonometry Problem Set")
                .description("Solve page 10-15 exercises")
                .instructions("Submit neat handwriting")
                .assignedDate(LocalDate.now().minusDays(2))
                .dueDate(LocalDate.now().plusDays(3))
                .maxMarks(BigDecimal.valueOf(100.0))
                .status(AssignmentStatus.PUBLISHED)
                .submissionStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        AuthContextHolder.clear();
    }

    @Test
    void student_GetMyHomework_Success() throws Exception {
        AuthenticatedUser studentUser = new AuthenticatedUser(50L, 1000L, UserRole.STUDENT);
        AuthContextHolder.set(studentUser);
        when(authContextService.requireCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(50L)).thenReturn(List.of(mockStudent));
        when(homeworkService.getStudentAssignments(eq(1000L), eq(17L), eq(AssignmentType.HOMEWORK)))
                .thenReturn(List.of(mockAssignment));

        mockMvc.perform(get("/api/v1/student/homework")
                        .param("type", "HOMEWORK")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Trigonometry Problem Set"))
                .andExpect(jsonPath("$.data[0].subjectName").value("Mathematics"));
    }

    @Test
    void parent_GetLinkedChildHomework_Success() throws Exception {
        AuthenticatedUser parentUser = new AuthenticatedUser(4L, 1000L, UserRole.PARENT);
        AuthContextHolder.set(parentUser);
        when(authContextService.requireCurrentUser()).thenReturn(parentUser);

        StudentParent link = new StudentParent();
        link.setId(new StudentParentId(mockStudent.getId(), 4L));
        link.setStudent(mockStudent);

        when(studentParentRepository.findByIdParentUserId(4L)).thenReturn(List.of(link));
        when(homeworkService.getStudentAssignments(eq(1000L), eq(17L), any()))
                .thenReturn(List.of(mockAssignment));

        mockMvc.perform(get("/api/v1/student/homework")
                        .param("studentId", "17")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].submissionStatus").value("PENDING"));
    }

    @Test
    void submitHomework_ValidRequest_Success() throws Exception {
        AuthenticatedUser studentUser = new AuthenticatedUser(50L, 1000L, UserRole.STUDENT);
        AuthContextHolder.set(studentUser);
        when(authContextService.requireCurrentUser()).thenReturn(studentUser);
        when(studentRepository.findByUserId(50L)).thenReturn(List.of(mockStudent));

        HomeworkSubmissionRequest req = new HomeworkSubmissionRequest();
        req.setSubmissionText("Solved all trigonometry questions.");

        HomeworkSubmissionResponse res = HomeworkSubmissionResponse.builder()
                .id(101L)
                .assignmentId(1L)
                .studentId(17L)
                .studentName("Rahul Patel")
                .status(SubmissionStatus.SUBMITTED)
                .submissionDate(LocalDateTime.now())
                .build();

        when(homeworkService.submitHomework(eq(1000L), eq(1L), eq(17L), any(HomeworkSubmissionRequest.class)))
                .thenReturn(res);

        mockMvc.perform(post("/api/v1/student/homework/1/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }
}
