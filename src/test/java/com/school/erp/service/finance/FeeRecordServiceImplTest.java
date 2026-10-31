package com.school.erp.service.finance;

import com.school.erp.dto.payment.FeeStatsDTO;
import com.school.erp.dto.payment.StudentFeeRecordDTO;
import com.school.erp.dto.payment.TeacherClassResponse;
import com.school.erp.entity.auth.PermissionDefinition;
import com.school.erp.entity.auth.RolePermission;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.student.Student;
import com.school.erp.repository.finance.FeeDueRepository;
import com.school.erp.repository.finance.StudentFeeAggregation;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.finance.impl.FeeRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeRecordServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FeeDueRepository feeDueRepository;

    @Mock
    private AuthContextService authContextService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private TeacherFeeService teacherFeeService;

    private FeeRecordServiceImpl feeRecordService;

    @BeforeEach
    void setUp() {
        feeRecordService = new FeeRecordServiceImpl(
                studentRepository,
                feeDueRepository,
                authContextService,
                authorizationService,
                teacherFeeService
        );
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
    }

    private RolePermission createRolePermission(String key, String scope) {
        RolePermission rp = new RolePermission();
        PermissionDefinition pd = new PermissionDefinition();
        pd.setPermissionKey(key);
        rp.setPermission(pd);
        rp.setScopeType(scope);
        return rp;
    }

    @Test
    void getStudentFeeRecords_WhenTeacherHasClassScope_RestrictsToAssignedClasses() {
        AuthenticatedUser teacherUser = new AuthenticatedUser(200L, 1L, UserRole.TEACHER);
        when(authContextService.getCurrentUserOrNull()).thenReturn(teacherUser);

        // Teacher has class scope on fees.search_due_fees.view
        when(authorizationService.getEffectivePermissions(1L, 200L)).thenReturn(List.of(
                createRolePermission("fees.search_due_fees.view", "class")
        ));

        // Teacher is assigned to class 10 and 11
        when(teacherFeeService.getAssignedClasses(1L)).thenReturn(List.of(
                new TeacherClassResponse(1L, 10L, "Class 10", 1L, "A"),
                new TeacherClassResponse(2L, 11L, "Class 11", 2L, "B")
        ));

        Student student = new Student();
        student.setId(101L);
        student.setFirstName("Rohan");
        student.setLastName("Verma");
        SchoolClass sc = new SchoolClass();
        sc.setName("Class 10");
        student.setSchoolClass(sc);

        when(studentRepository.findBySchoolIdAndSearchAndClassIdIn(eq(1L), eq(""), eq(List.of(10L, 11L)), any()))
                .thenReturn(new PageImpl<>(List.of(student)));

        StudentFeeAggregation agg = mock(StudentFeeAggregation.class);
        when(agg.getStudentId()).thenReturn(101L);
        when(agg.getTotalFees()).thenReturn(new BigDecimal("12000.00"));
        when(agg.getAmountPaid()).thenReturn(new BigDecimal("8000.00"));
        when(feeDueRepository.aggregateFeesByStudentIds(List.of(101L))).thenReturn(List.of(agg));

        Page<StudentFeeRecordDTO> result = feeRecordService.getStudentFeeRecords(1L, null, "", null, PageRequest.of(0, 50));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStudentName()).isEqualTo("Rohan Verma");
        assertThat(result.getContent().get(0).getBalance()).isEqualByComparingTo("4000.00");

        // Verify repository called with assigned classes
        verify(studentRepository).findBySchoolIdAndSearchAndClassIdIn(eq(1L), eq(""), eq(List.of(10L, 11L)), any());
        verify(studentRepository, never()).findBySchoolIdAndSearchAndClassIdAndSectionId(any(), any(), any(), any(), any());
    }

    @Test
    void getStudentFeeRecords_WhenTeacherRequestsUnassignedClass_ReturnsEmpty() {
        AuthenticatedUser teacherUser = new AuthenticatedUser(200L, 1L, UserRole.TEACHER);
        when(authContextService.getCurrentUserOrNull()).thenReturn(teacherUser);

        when(authorizationService.getEffectivePermissions(1L, 200L)).thenReturn(List.of(
                createRolePermission("fees.search_due_fees.view", "class")
        ));

        // Teacher is only assigned to Class 10
        when(teacherFeeService.getAssignedClasses(1L)).thenReturn(List.of(
                new TeacherClassResponse(1L, 10L, "Class 10", 1L, "A")
        ));

        // Teacher attempts to query Class 99
        Page<StudentFeeRecordDTO> result = feeRecordService.getStudentFeeRecords(1L, 99L, "", null, PageRequest.of(0, 50));

        assertThat(result.getContent()).isEmpty();
        verifyNoInteractions(studentRepository);
    }

    @Test
    void getStudentFeeRecords_WhenAdminHasSchoolScope_AllowsAllClasses() {
        AuthenticatedUser adminUser = new AuthenticatedUser(1L, 1L, UserRole.ADMIN);
        when(authContextService.getCurrentUserOrNull()).thenReturn(adminUser);

        when(authorizationService.getEffectivePermissions(1L, 1L)).thenReturn(List.of(
                createRolePermission("fees.search_due_fees.view", "school")
        ));

        Student student = new Student();
        student.setId(201L);
        student.setFirstName("Pooja");
        student.setLastName("Patel");

        when(studentRepository.findBySchoolIdAndSearchAndClassIdAndSectionId(eq(1L), eq(""), eq(null), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(student)));

        StudentFeeAggregation agg = mock(StudentFeeAggregation.class);
        when(agg.getStudentId()).thenReturn(201L);
        when(agg.getTotalFees()).thenReturn(new BigDecimal("15000.00"));
        when(agg.getAmountPaid()).thenReturn(new BigDecimal("15000.00"));
        when(feeDueRepository.aggregateFeesByStudentIds(List.of(201L))).thenReturn(List.of(agg));

        Page<StudentFeeRecordDTO> result = feeRecordService.getStudentFeeRecords(1L, null, "", null, PageRequest.of(0, 50));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo("PAID");
        verify(studentRepository).findBySchoolIdAndSearchAndClassIdAndSectionId(eq(1L), eq(""), eq(null), eq(null), any());
    }

    @Test
    void getFeeStats_WhenTeacherHasClassScope_AggregatesOnlyAssignedClasses() {
        AuthenticatedUser teacherUser = new AuthenticatedUser(200L, 1L, UserRole.TEACHER);
        when(authContextService.getCurrentUserOrNull()).thenReturn(teacherUser);

        when(authorizationService.getEffectivePermissions(1L, 200L)).thenReturn(List.of(
                createRolePermission("fees.search_due_fees.view", "class")
        ));

        when(teacherFeeService.getAssignedClasses(1L)).thenReturn(List.of(
                new TeacherClassResponse(1L, 10L, "Class 10", 1L, "A")
        ));

        StudentFeeAggregation agg = mock(StudentFeeAggregation.class);
        when(agg.getTotalFees()).thenReturn(new BigDecimal("100000.00"));
        when(agg.getAmountPaid()).thenReturn(new BigDecimal("70000.00"));

        when(feeDueRepository.aggregateClassAndSectionFeeStats(eq(1L), eq(List.of(10L)), isNull())).thenReturn(agg);

        FeeStatsDTO stats = feeRecordService.getFeeStats(1L, null);

        assertThat(stats.getTotalExpected()).isEqualByComparingTo("100000.00");
        assertThat(stats.getTotalCollected()).isEqualByComparingTo("70000.00");
        assertThat(stats.getOverdueBalance()).isEqualByComparingTo("30000.00");

        verify(feeDueRepository).aggregateClassAndSectionFeeStats(eq(1L), eq(List.of(10L)), isNull());
        verify(feeDueRepository, never()).aggregateSchoolFeeStats(any());
    }
}
