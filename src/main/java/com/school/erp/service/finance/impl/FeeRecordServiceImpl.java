package com.school.erp.service.finance.impl;

import com.school.erp.dto.payment.FeeStatsDTO;
import com.school.erp.dto.payment.StudentFeeRecordDTO;
import com.school.erp.dto.payment.TeacherClassResponse;
import com.school.erp.entity.auth.RolePermission;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.student.Student;
import com.school.erp.repository.finance.FeeDueRepository;
import com.school.erp.repository.finance.StudentFeeAggregation;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.finance.FeeRecordService;
import com.school.erp.service.finance.TeacherFeeService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FeeRecordServiceImpl implements FeeRecordService {

    private final StudentRepository studentRepository;
    private final FeeDueRepository feeDueRepository;
    private final AuthContextService authContextService;
    private final AuthorizationService authorizationService;
    private final TeacherFeeService teacherFeeService;

    public FeeRecordServiceImpl(
            StudentRepository studentRepository,
            FeeDueRepository feeDueRepository,
            AuthContextService authContextService,
            @Lazy AuthorizationService authorizationService,
            @Lazy TeacherFeeService teacherFeeService) {
        this.studentRepository = studentRepository;
        this.feeDueRepository = feeDueRepository;
        this.authContextService = authContextService;
        this.authorizationService = authorizationService;
        this.teacherFeeService = teacherFeeService;
    }

    private boolean hasSchoolScope(Long schoolId, AuthenticatedUser currentUser) {
        if (currentUser == null) return false;
        if (currentUser.role() == UserRole.SUPER_ADMIN) return true;
        if (authorizationService == null) return true;

        List<RolePermission> effective = authorizationService.getEffectivePermissions(schoolId, currentUser.userId());
        return effective.stream().anyMatch(rp ->
                "fees.search_due_fees.view".equals(rp.getPermission().getPermissionKey()) &&
                "school".equalsIgnoreCase(rp.getScopeType())
        );
    }

    @Override
    public Page<StudentFeeRecordDTO> getStudentFeeRecords(Long schoolId, Long classId, String search, String status, Pageable pageable) {
        String safeSearch = (search == null) ? "" : search;
        AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        boolean schoolScope = hasSchoolScope(effectiveSchoolId, currentUser);

        Page<Student> students;
        if (schoolScope) {
            students = studentRepository.findBySchoolIdAndSearchAndClassId(effectiveSchoolId, safeSearch, classId, pageable);
        } else {
            List<Long> assignedClassIds = teacherFeeService.getAssignedClasses(effectiveSchoolId).stream()
                    .map(TeacherClassResponse::getClassId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (assignedClassIds.isEmpty()) {
                return Page.empty(pageable);
            }

            if (classId != null) {
                if (!assignedClassIds.contains(classId)) {
                    return Page.empty(pageable);
                }
                students = studentRepository.findBySchoolIdAndSearchAndClassId(effectiveSchoolId, safeSearch, classId, pageable);
            } else {
                students = studentRepository.findBySchoolIdAndSearchAndClassIdIn(effectiveSchoolId, safeSearch, assignedClassIds, pageable);
            }
        }

        List<Long> studentIds = students.stream().map(Student::getId).collect(Collectors.toList());

        if (studentIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<StudentFeeAggregation> aggregations = feeDueRepository.aggregateFeesByStudentIds(studentIds);
        Map<Long, StudentFeeAggregation> aggMap = aggregations.stream()
                .collect(Collectors.toMap(StudentFeeAggregation::getStudentId, a -> a));

        return students.map(student -> {
            StudentFeeAggregation agg = aggMap.get(student.getId());
            BigDecimal totalFees = agg != null && agg.getTotalFees() != null ? agg.getTotalFees() : BigDecimal.ZERO;
            BigDecimal amountPaid = agg != null && agg.getAmountPaid() != null ? agg.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal balance = totalFees.subtract(amountPaid);

            String derivedStatus = "PENDING";
            if (totalFees.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(BigDecimal.ZERO) <= 0) {
                derivedStatus = "PAID";
            } else if (amountPaid.compareTo(BigDecimal.ZERO) > 0) {
                derivedStatus = "PARTIAL";
            }

            return new StudentFeeRecordDTO(
                    String.valueOf(student.getId()),
                    String.valueOf(student.getId()),
                    student.getFirstName() + " " + student.getLastName(),
                    student.getAdmissionNo(),
                    student.getSchoolClass() != null ? student.getSchoolClass().getName() : "-",
                    null,
                    totalFees,
                    amountPaid,
                    balance,
                    null,
                    derivedStatus
            );
        });
    }

    @Override
    public FeeStatsDTO getFeeStats(Long schoolId, Long classId) {
        AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        boolean schoolScope = hasSchoolScope(effectiveSchoolId, currentUser);

        StudentFeeAggregation agg;
        if (schoolScope) {
            if (classId != null) {
                agg = feeDueRepository.aggregateClassFeeStats(effectiveSchoolId, List.of(classId));
            } else {
                agg = feeDueRepository.aggregateSchoolFeeStats(effectiveSchoolId);
            }
        } else {
            List<Long> assignedClassIds = teacherFeeService.getAssignedClasses(effectiveSchoolId).stream()
                    .map(TeacherClassResponse::getClassId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (assignedClassIds.isEmpty()) {
                return new FeeStatsDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
            }

            if (classId != null) {
                if (!assignedClassIds.contains(classId)) {
                    return new FeeStatsDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
                }
                agg = feeDueRepository.aggregateClassFeeStats(effectiveSchoolId, List.of(classId));
            } else {
                agg = feeDueRepository.aggregateClassFeeStats(effectiveSchoolId, assignedClassIds);
            }
        }

        BigDecimal totalExpected = agg != null && agg.getTotalFees() != null ? agg.getTotalFees() : BigDecimal.ZERO;
        BigDecimal totalCollected = agg != null && agg.getAmountPaid() != null ? agg.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal overdueBalance = totalExpected.subtract(totalCollected);

        return new FeeStatsDTO(
                totalExpected,
                totalCollected,
                BigDecimal.ZERO,
                overdueBalance.max(BigDecimal.ZERO),
                0L
        );
    }
}
