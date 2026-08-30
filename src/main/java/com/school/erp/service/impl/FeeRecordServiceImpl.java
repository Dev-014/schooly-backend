package com.school.erp.service.impl;

import com.school.erp.dto.payment.FeeStatsDTO;
import com.school.erp.dto.payment.StudentFeeRecordDTO;
import com.school.erp.entity.Student;
import com.school.erp.repository.FeeDueRepository;
import com.school.erp.repository.StudentFeeAggregation;
import com.school.erp.repository.StudentRepository;
import com.school.erp.service.FeeRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FeeRecordServiceImpl implements FeeRecordService {

    private final StudentRepository studentRepository;
    private final FeeDueRepository feeDueRepository;

    public FeeRecordServiceImpl(StudentRepository studentRepository, FeeDueRepository feeDueRepository) {
        this.studentRepository = studentRepository;
        this.feeDueRepository = feeDueRepository;
    }

    @Override
    public Page<StudentFeeRecordDTO> getStudentFeeRecords(Long schoolId, Long classId, String search, String status, Pageable pageable) {
        String safeSearch = (search == null) ? "" : search;
        Page<Student> students = studentRepository.findBySchoolIdAndSearchAndClassId(schoolId, safeSearch, classId, pageable);
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
                    null, // lastPaymentDate could be queried separately if needed
                    derivedStatus
            );
        });
    }

    @Override
    public FeeStatsDTO getFeeStats(Long schoolId) {
        StudentFeeAggregation agg = feeDueRepository.aggregateSchoolFeeStats(schoolId);
        
        BigDecimal totalExpected = agg != null && agg.getTotalFees() != null ? agg.getTotalFees() : BigDecimal.ZERO;
        BigDecimal totalCollected = agg != null && agg.getAmountPaid() != null ? agg.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal overdueBalance = totalExpected.subtract(totalCollected);

        // For simplicity, returning mock partial stats or 0
        return new FeeStatsDTO(
                totalExpected,
                totalCollected,
                BigDecimal.ZERO,
                overdueBalance.max(BigDecimal.ZERO),
                0L
        );
    }
}
