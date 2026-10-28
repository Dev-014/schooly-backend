package com.school.erp.service.student;

import com.school.erp.dto.academic.ClassTeacherAssignmentResponse;
import com.school.erp.dto.student.StudentLeaveRequest;
import com.school.erp.dto.student.StudentLeaveResponse;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentLeave;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.ForbiddenException;
import com.school.erp.repository.student.StudentLeaveRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.academic.AcademicService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentLeaveServiceTest {

    @Mock
    private StudentLeaveRepository leaveRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private AuthContextService authContextService;

    @Mock
    private AcademicService academicService;

    @InjectMocks
    private StudentLeaveService leaveService;

    private School school;
    private SchoolClass class10;
    private SchoolClass class7;
    private Student student10;
    private Student student7;
    private StudentLeave leave10;
    private StudentLeave leave7;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);

        class10 = new SchoolClass();
        class10.setId(10L);
        class10.setName("Grade 10");

        class7 = new SchoolClass();
        class7.setId(7L);
        class7.setName("Grade 7");

        student10 = new Student();
        student10.setId(100L);
        student10.setFirstName("Atul");
        student10.setLastName("Yadav");
        student10.setSchool(school);
        student10.setSchoolClass(class10);
        student10.setSectionId(1L);

        student7 = new Student();
        student7.setId(107L);
        student7.setFirstName("Rahul");
        student7.setLastName("Sharma");
        student7.setSchool(school);
        student7.setSchoolClass(class7);
        student7.setSectionId(1L);

        leave10 = new StudentLeave();
        leave10.setId(1L);
        leave10.setSchool(school);
        leave10.setStudent(student10);
        leave10.setApplyDate(LocalDate.now());
        leave10.setFromDate(LocalDate.now().plusDays(1));
        leave10.setToDate(LocalDate.now().plusDays(2));
        leave10.setDays(2);
        leave10.setStatus("PENDING");

        leave7 = new StudentLeave();
        leave7.setId(2L);
        leave7.setSchool(school);
        leave7.setStudent(student7);
        leave7.setApplyDate(LocalDate.now());
        leave7.setFromDate(LocalDate.now().plusDays(1));
        leave7.setToDate(LocalDate.now().plusDays(3));
        leave7.setDays(3);
        leave7.setStatus("PENDING");
    }

    @AfterEach
    void tearDown() {
        AuthContextHolder.clear();
    }

    @Test
    void getAllLeaves_asTeacher_filtersLeavesByAssignedClass() {
        AuthenticatedUser teacherUser = new AuthenticatedUser(50L, 1L, UserRole.TEACHER);
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(authContextService.getCurrentUserOrNull()).thenReturn(teacherUser);
        when(leaveRepository.findBySchoolId(1L)).thenReturn(List.of(leave10, leave7));

        ClassTeacherAssignmentResponse assignment = new ClassTeacherAssignmentResponse(
                1L, 1L, 50L, "Teacher", "teacher@test.com", 10L, "Grade 10", 1L, "Section A", 1L, "2025-26", "ACTIVE"
        );
        when(academicService.getMyClassTeacherAssignments(1L)).thenReturn(List.of(assignment));

        List<StudentLeaveResponse> result = leaveService.getAllLeaves(1L, null);

        assertEquals(1, result.size());
        assertEquals("Atul Yadav", result.get(0).studentName());
        assertEquals(10L, result.get(0).classId());
    }

    @Test
    void updateLeave_asTeacher_outsideClass_throwsForbidden() {
        AuthenticatedUser teacherUser = new AuthenticatedUser(50L, 1L, UserRole.TEACHER);
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(authContextService.getCurrentUserOrNull()).thenReturn(teacherUser);
        when(leaveRepository.findByIdAndSchoolId(2L, 1L)).thenReturn(Optional.of(leave7));

        ClassTeacherAssignmentResponse assignment = new ClassTeacherAssignmentResponse(
                1L, 1L, 50L, "Teacher", "teacher@test.com", 10L, "Grade 10", 1L, "Section A", 1L, "2025-26", "ACTIVE"
        );
        when(academicService.getMyClassTeacherAssignments(1L)).thenReturn(List.of(assignment));

        StudentLeaveRequest req = new StudentLeaveRequest(
                1L, 107L, LocalDate.now(), LocalDate.now().plusDays(2), 2, "Trip", "APPROVED", "Approved"
        );

        assertThrows(ForbiddenException.class, () -> leaveService.updateLeave(2L, req));
    }

    @Test
    void updateLeave_asTeacher_inAssignedClass_succeeds() {
        AuthenticatedUser teacherUser = new AuthenticatedUser(50L, 1L, UserRole.TEACHER);
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(authContextService.getCurrentUserOrNull()).thenReturn(teacherUser);
        when(leaveRepository.findByIdAndSchoolId(1L, 1L)).thenReturn(Optional.of(leave10));
        when(leaveRepository.save(any(StudentLeave.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClassTeacherAssignmentResponse assignment = new ClassTeacherAssignmentResponse(
                1L, 1L, 50L, "Teacher", "teacher@test.com", 10L, "Grade 10", 1L, "Section A", 1L, "2025-26", "ACTIVE"
        );
        when(academicService.getMyClassTeacherAssignments(1L)).thenReturn(List.of(assignment));

        StudentLeaveRequest req = new StudentLeaveRequest(
                1L, 100L, LocalDate.now(), LocalDate.now().plusDays(2), 2, "Medical", "APPROVED", "Approved"
        );

        StudentLeaveResponse resp = leaveService.updateLeave(1L, req);

        assertNotNull(resp);
        assertEquals("APPROVED", resp.status());
        assertEquals("Approved", resp.reply());
    }

    @Test
    void createLeave_asTeacher_inAssignedClass_succeeds() {
        AuthenticatedUser teacherUser = new AuthenticatedUser(50L, 1L, UserRole.TEACHER);
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(authContextService.getCurrentUserOrNull()).thenReturn(teacherUser);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(studentRepository.findById(100L)).thenReturn(Optional.of(student10));
        when(leaveRepository.save(any(StudentLeave.class))).thenAnswer(invocation -> {
            StudentLeave sl = invocation.getArgument(0);
            sl.setId(10L);
            return sl;
        });

        ClassTeacherAssignmentResponse assignment = new ClassTeacherAssignmentResponse(
                1L, 1L, 50L, "Teacher", "teacher@test.com", 10L, "Grade 10", 1L, "Section A", 1L, "2025-26", "ACTIVE"
        );
        when(academicService.getMyClassTeacherAssignments(1L)).thenReturn(List.of(assignment));

        StudentLeaveRequest req = new StudentLeaveRequest(
                1L, 100L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 2, "Medical", "PENDING", null
        );

        StudentLeaveResponse resp = leaveService.createLeave(req);
        assertNotNull(resp);
        assertEquals(10L, resp.id());
        assertEquals("Atul Yadav", resp.studentName());
        assertEquals(10L, resp.classId());
    }

    @Test
    void createLeave_asTeacher_outsideClass_throwsForbidden() {
        AuthenticatedUser teacherUser = new AuthenticatedUser(50L, 1L, UserRole.TEACHER);
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(authContextService.getCurrentUserOrNull()).thenReturn(teacherUser);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));
        when(studentRepository.findById(107L)).thenReturn(Optional.of(student7));

        ClassTeacherAssignmentResponse assignment = new ClassTeacherAssignmentResponse(
                1L, 1L, 50L, "Teacher", "teacher@test.com", 10L, "Grade 10", 1L, "Section A", 1L, "2025-26", "ACTIVE"
        );
        when(academicService.getMyClassTeacherAssignments(1L)).thenReturn(List.of(assignment));

        StudentLeaveRequest req = new StudentLeaveRequest(
                1L, 107L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 2, "Trip", "PENDING", null
        );

        assertThrows(ForbiddenException.class, () -> leaveService.createLeave(req));
    }
}

