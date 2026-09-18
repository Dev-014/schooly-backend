package com.school.erp.repository;

import com.school.erp.entity.FeeDue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeDueRepository extends JpaRepository<FeeDue, Long> {
    List<FeeDue> findByStudentIdAndSchoolIdOrderByDueDateAsc(Long studentId, Long schoolId);
    List<FeeDue> findByStudentIdAndSchoolIdAndStatusInOrderByDueDateAsc(Long studentId, Long schoolId, List<String> statuses);
    boolean existsByStudentIdAndFeeStructureIdAndIsAdHocFalse(Long studentId, Long feeStructureId);

    @org.springframework.data.jpa.repository.Query("SELECT d.student.id AS studentId, SUM(d.amount) AS totalFees, SUM(d.paidAmount) AS amountPaid " +
           "FROM FeeDue d WHERE d.student.id IN :studentIds GROUP BY d.student.id")
    List<StudentFeeAggregation> aggregateFeesByStudentIds(@org.springframework.data.repository.query.Param("studentIds") List<Long> studentIds);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.amount) AS totalFees, SUM(d.paidAmount) AS amountPaid " +
           "FROM FeeDue d WHERE d.school.id = :schoolId")
    StudentFeeAggregation aggregateSchoolFeeStats(@org.springframework.data.repository.query.Param("schoolId") Long schoolId);
}
