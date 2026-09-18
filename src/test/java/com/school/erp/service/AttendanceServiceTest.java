package com.school.erp.service;

import com.school.erp.dto.attendance.AttendanceSummaryDTO;
import com.school.erp.dto.attendance.analytics.AttendanceTrendDTO;
import com.school.erp.dto.attendance.analytics.GradeAttendanceDTO;
import com.school.erp.entity.SchoolClass;
import com.school.erp.repository.*;
import com.school.erp.repository.auth.UserAssignmentRepository;
import com.school.erp.security.AuthContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private SchoolRepository schoolRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private SchoolClassRepository schoolClassRepository;
    @Mock
    private StudentLeaveRepository studentLeaveRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private ClassTeacherAssignmentRepository assignmentRepository;
    @Mock
    private UserAssignmentRepository userAssignmentRepository;
    @Mock
    private AuthContextService authContextService;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(
                attendanceRepository,
                schoolRepository,
                studentRepository,
                schoolClassRepository,
                studentLeaveRepository,
                staffRepository,
                assignmentRepository,
                userAssignmentRepository,
                authContextService
        );
    }

    @Test
    void getSummaryToday_ShouldCalculateCorrectMetrics() {
        Long schoolId = 1L;
        LocalDate today = LocalDate.of(2026, 9, 13);
        when(authContextService.resolveSchoolId(schoolId)).thenReturn(schoolId);
        when(studentRepository.countBySchoolId(schoolId)).thenReturn(100L);
        when(attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(schoolId, today, "PRESENT")).thenReturn(85L);
        when(attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(schoolId, today, "ABSENT")).thenReturn(10L);
        when(attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(schoolId, today, "LATE")).thenReturn(5L);
        when(attendanceRepository.countBySchoolIdAndAttendanceDateAndStatus(schoolId, today, "EXCUSED")).thenReturn(0L);
        when(studentLeaveRepository.countBySchoolIdAndStatus(schoolId, "PENDING")).thenReturn(3L);

        AttendanceSummaryDTO summary = attendanceService.getSummaryToday(schoolId, today);

        assertThat(summary.totalStudents()).isEqualTo(100);
        assertThat(summary.present()).isEqualTo(85);
        assertThat(summary.absent()).isEqualTo(10);
        assertThat(summary.late()).isEqualTo(5);
        assertThat(summary.presentPercent()).isEqualTo(90); // (85 + 5) / 100
        assertThat(summary.pendingLeaves()).isEqualTo(3);
    }

    @Test
    void getAttendanceTrends_ShouldMapQueryResults() {
        Long schoolId = 1L;
        when(authContextService.resolveSchoolId(schoolId)).thenReturn(schoolId);
        LocalDate d1 = LocalDate.of(2026, 9, 10);
        LocalDate d2 = LocalDate.of(2026, 9, 11);
        List<Object[]> raw = new java.util.ArrayList<>();
        raw.add(new Object[]{d1, 90L, 100L});
        raw.add(new Object[]{d2, 95L, 100L});
        when(attendanceRepository.getAttendanceTrendsByDate(eq(schoolId), any(LocalDate.class))).thenReturn(raw);

        List<AttendanceTrendDTO> trends = attendanceService.getAttendanceTrends(schoolId, 7);

        assertThat(trends).hasSize(2);
        assertThat(trends.get(0).presentPercent()).isEqualTo(90);
        assertThat(trends.get(1).presentPercent()).isEqualTo(95);
    }

    @Test
    void getGradeWiseAttendance_ShouldIncludeAllClasses() {
        Long schoolId = 1L;
        when(authContextService.resolveSchoolId(schoolId)).thenReturn(schoolId);

        List<Object[]> raw = new java.util.ArrayList<>();
        raw.add(new Object[]{"Grade 10-A", 30L, 27.0, 2.0, 30L});
        when(attendanceRepository.getGradeWiseAttendance(schoolId)).thenReturn(raw);

        SchoolClass classA = new SchoolClass();
        classA.setId(1L);
        classA.setName("Grade 10-A");

        SchoolClass classB = new SchoolClass();
        classB.setId(2L);
        classB.setName("Grade 11-B");

        when(schoolClassRepository.findBySchoolId(schoolId)).thenReturn(List.of(classA, classB));
        when(studentRepository.findBySchoolIdAndSchoolClassId(schoolId, 2L)).thenReturn(List.of());

        List<GradeAttendanceDTO> gradeWise = attendanceService.getGradeWiseAttendance(schoolId);

        assertThat(gradeWise).hasSize(2);
        assertThat(gradeWise.get(0).className()).isEqualTo("Grade 10-A");
        assertThat(gradeWise.get(0).performance()).isEqualTo("EXCELLENT");
        assertThat(gradeWise.get(1).className()).isEqualTo("Grade 11-B");
    }

    @Test
    void createAttendance_WhenTeacherAssignedToDifferentSection_ShouldThrowForbiddenException() {
        Long schoolId = 4L;
        Long teacherUserId = 125L;
        Long classId = 5L;
        Long sectionAId = 6L;
        Long studentId = 50L;

        when(authContextService.resolveSchoolId(schoolId)).thenReturn(schoolId);
        when(authContextService.getCurrentUserOrNull()).thenReturn(
                new com.school.erp.security.AuthenticatedUser(
                        teacherUserId,
                        schoolId,
                        com.school.erp.entity.UserRole.TEACHER
                )
        );

        com.school.erp.entity.School school = new com.school.erp.entity.School();
        school.setId(schoolId);
        when(schoolRepository.findById(schoolId)).thenReturn(java.util.Optional.of(school));

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(classId);

        com.school.erp.entity.Student student = new com.school.erp.entity.Student();
        student.setId(studentId);
        student.setSchool(school);
        student.setSchoolClass(schoolClass);
        student.setSectionId(sectionAId);
        when(studentRepository.findByIdAndSchoolId(studentId, schoolId)).thenReturn(java.util.Optional.of(student));

        when(staffRepository.findByUserId(teacherUserId)).thenReturn(java.util.Optional.empty());
        when(userAssignmentRepository.existsBySchoolIdAndUserIdAndAssignmentTypeAndClassIdAndSectionIdAndIsActiveTrue(
                schoolId, teacherUserId, "class_teacher", classId, sectionAId
        )).thenReturn(false);
        when(userAssignmentRepository.existsBySchoolIdAndUserIdAndAssignmentTypeAndClassIdAndSectionIdIsNullAndIsActiveTrue(
                schoolId, teacherUserId, "class_teacher", classId
        )).thenReturn(false);

        com.school.erp.dto.attendance.AttendanceRequest request = new com.school.erp.dto.attendance.AttendanceRequest(
                studentId,
                schoolId,
                LocalDate.now(),
                "PRESENT"
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                com.school.erp.exception.ForbiddenException.class,
                () -> attendanceService.createAttendance(request)
        );
    }

    @Test
    void createAttendance_WhenTeacherAssignedToMatchingSection_ShouldSucceed() {
        Long schoolId = 4L;
        Long teacherUserId = 125L;
        Long classId = 5L;
        Long sectionBId = 7L;
        Long studentId = 51L;

        when(authContextService.resolveSchoolId(schoolId)).thenReturn(schoolId);
        when(authContextService.getCurrentUserOrNull()).thenReturn(
                new com.school.erp.security.AuthenticatedUser(
                        teacherUserId,
                        schoolId,
                        com.school.erp.entity.UserRole.TEACHER
                )
        );

        com.school.erp.entity.School school = new com.school.erp.entity.School();
        school.setId(schoolId);
        when(schoolRepository.findById(schoolId)).thenReturn(java.util.Optional.of(school));

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(classId);

        com.school.erp.entity.Student student = new com.school.erp.entity.Student();
        student.setId(studentId);
        student.setSchool(school);
        student.setSchoolClass(schoolClass);
        student.setSectionId(sectionBId);
        when(studentRepository.findByIdAndSchoolId(studentId, schoolId)).thenReturn(java.util.Optional.of(student));

        when(staffRepository.findByUserId(teacherUserId)).thenReturn(java.util.Optional.empty());
        when(userAssignmentRepository.existsBySchoolIdAndUserIdAndAssignmentTypeAndClassIdAndSectionIdAndIsActiveTrue(
                schoolId, teacherUserId, "class_teacher", classId, sectionBId
        )).thenReturn(true);

        when(attendanceRepository.save(any(com.school.erp.entity.Attendance.class))).thenAnswer(invocation -> {
            com.school.erp.entity.Attendance a = invocation.getArgument(0);
            a.setId(999L);
            return a;
        });

        com.school.erp.dto.attendance.AttendanceRequest request = new com.school.erp.dto.attendance.AttendanceRequest(
                studentId,
                schoolId,
                LocalDate.now(),
                "PRESENT"
        );

        com.school.erp.dto.attendance.AttendanceResponse response = attendanceService.createAttendance(request);
        assertThat(response).isNotNull();
        assertThat(response.studentId()).isEqualTo(studentId);
        assertThat(response.status()).isEqualTo("PRESENT");
    }
}
