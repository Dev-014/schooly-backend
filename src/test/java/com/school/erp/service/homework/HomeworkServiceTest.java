package com.school.erp.service.homework;

import com.school.erp.dto.homework.*;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Subject;
import com.school.erp.entity.homework.*;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.academic.SchoolClassRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.academic.SubjectRepository;
import com.school.erp.repository.homework.HomeworkAssignmentRepository;
import com.school.erp.repository.homework.HomeworkSubmissionRepository;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeworkServiceTest {

    @Mock
    private HomeworkAssignmentRepository assignmentRepository;
    @Mock
    private HomeworkSubmissionRepository submissionRepository;
    @Mock
    private SchoolRepository schoolRepository;
    @Mock
    private SchoolClassRepository classRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AuthContextService authContextService;

    @InjectMocks
    private HomeworkService homeworkService;

    private School school;
    private SchoolClass schoolClass;
    private Subject subject;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);

        schoolClass = new SchoolClass();
        schoolClass.setId(10L);
        schoolClass.setName("Grade 10");

        subject = new Subject();
        subject.setId(20L);
        subject.setName("Mathematics");
    }

    @Test
    void createAssignment_DueDateBeforeAssignedDate_ShouldThrowBadRequest() {
        when(authContextService.resolveSchoolId(any())).thenReturn(1L);

        HomeworkAssignmentRequest req = HomeworkAssignmentRequest.builder()
                .classId(10L)
                .subjectId(20L)
                .title("Invalid Date Homework")
                .assignedDate(LocalDate.now())
                .dueDate(LocalDate.now().minusDays(1)) // due date before assigned date!
                .build();

        assertThrows(BadRequestException.class, () -> homeworkService.createAssignment(1L, req));
    }

    @Test
    void createAssignment_ValidRequest_ShouldSaveSuccessfully() {
        when(authContextService.resolveSchoolId(any())).thenReturn(1L);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(classRepository.findByIdAndSchoolId(10L, 1L)).thenReturn(Optional.of(schoolClass));
        when(subjectRepository.findByIdAndSchoolId(20L, 1L)).thenReturn(Optional.of(subject));

        HomeworkAssignment savedAssignment = HomeworkAssignment.builder()
                .id(101L)
                .school(school)
                .schoolClass(schoolClass)
                .subject(subject)
                .title("Trigonometry Practice")
                .assignedDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(3))
                .assignmentType(AssignmentType.HOMEWORK)
                .status(AssignmentStatus.PUBLISHED)
                .build();

        when(assignmentRepository.save(any(HomeworkAssignment.class))).thenReturn(savedAssignment);

        HomeworkAssignmentRequest req = HomeworkAssignmentRequest.builder()
                .classId(10L)
                .subjectId(20L)
                .title("Trigonometry Practice")
                .assignedDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(3))
                .build();

        HomeworkAssignmentResponse response = homeworkService.createAssignment(1L, req);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Trigonometry Practice", response.getTitle());
        verify(assignmentRepository, times(1)).save(any(HomeworkAssignment.class));
    }

    @Test
    void submitHomework_WhenPastDueDate_ShouldMarkAsLate() {
        when(authContextService.resolveSchoolId(any())).thenReturn(1L);

        HomeworkAssignment assignment = HomeworkAssignment.builder()
                .id(5L)
                .school(school)
                .dueDate(LocalDate.now().minusDays(2)) // Overdue!
                .build();

        Student student = new Student();
        student.setId(50L);
        student.setName("Bob Jones");

        when(assignmentRepository.findByIdAndSchoolId(5L, 1L)).thenReturn(Optional.of(assignment));
        when(studentRepository.findByIdAndSchoolId(50L, 1L)).thenReturn(Optional.of(student));
        when(submissionRepository.findByAssignmentIdAndStudentId(5L, 50L)).thenReturn(Optional.empty());

        when(submissionRepository.save(any(HomeworkSubmission.class))).thenAnswer(invocation -> {
            HomeworkSubmission sub = invocation.getArgument(0);
            sub.setId(500L);
            return sub;
        });

        HomeworkSubmissionRequest req = HomeworkSubmissionRequest.builder()
                .submissionText("Submitted late due to sickness")
                .build();

        HomeworkSubmissionResponse res = homeworkService.submitHomework(1L, 5L, 50L, req);

        assertNotNull(res);
        assertEquals(SubmissionStatus.LATE, res.getStatus());
    }

    @Test
    void evaluateSubmission_ExceedsMaxMarks_ShouldThrowBadRequest() {
        when(authContextService.resolveSchoolId(any())).thenReturn(1L);

        HomeworkAssignment assignment = HomeworkAssignment.builder()
                .id(5L)
                .maxMarks(BigDecimal.valueOf(20))
                .build();

        HomeworkSubmission submission = HomeworkSubmission.builder()
                .id(100L)
                .assignment(assignment)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        when(submissionRepository.findByIdAndSchoolId(100L, 1L)).thenReturn(Optional.of(submission));

        HomeworkEvaluationRequest evalReq = HomeworkEvaluationRequest.builder()
                .marksObtained(BigDecimal.valueOf(25)) // 25 > maxMarks(20)!
                .build();

        assertThrows(BadRequestException.class, () -> homeworkService.evaluateSubmission(1L, 100L, evalReq));
    }

    @Test
    void evaluateSubmission_WithGradedStatus_ShouldEvaluateSuccessfully() {
        when(authContextService.resolveSchoolId(any())).thenReturn(1L);

        HomeworkAssignment assignment = HomeworkAssignment.builder()
                .id(5L)
                .maxMarks(BigDecimal.valueOf(50))
                .build();

        HomeworkSubmission submission = HomeworkSubmission.builder()
                .id(100L)
                .school(school)
                .assignment(assignment)
                .status(SubmissionStatus.SUBMITTED)
                .build();

        when(submissionRepository.findByIdAndSchoolId(100L, 1L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(HomeworkSubmission.class))).thenAnswer(i -> i.getArgument(0));

        HomeworkEvaluationRequest evalReq = HomeworkEvaluationRequest.builder()
                .marksObtained(BigDecimal.valueOf(45))
                .status(SubmissionStatus.GRADED)
                .remarks("Excellent work!")
                .build();

        HomeworkSubmissionResponse res = homeworkService.evaluateSubmission(1L, 100L, evalReq);

        assertNotNull(res);
        assertEquals(SubmissionStatus.GRADED, res.getStatus());
        assertEquals(SubmissionStatus.GRADED, res.getSubmissionStatus());
        assertEquals(BigDecimal.valueOf(45), res.getMarksObtained());
    }
}
