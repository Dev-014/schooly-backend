package com.school.erp.repository.finance;

import com.school.erp.entity.finance.FeeDue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeDueRepository extends JpaRepository<FeeDue, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"feeCategory"})
    List<FeeDue> findByStudentIdAndSchoolIdOrderByDueDateAsc(Long studentId, Long schoolId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"feeCategory"})
    List<FeeDue> findByStudentIdAndSchoolIdAndStatusInOrderByDueDateAsc(Long studentId, Long schoolId, List<String> statuses);
    boolean existsByStudentIdAndFeeStructureIdAndIsAdHocFalse(Long studentId, Long feeStructureId);

    @org.springframework.data.jpa.repository.Query("SELECT d.student.id AS studentId, SUM(d.amount) AS totalFees, SUM(d.paidAmount) AS amountPaid, SUM(COALESCE(d.discountAmount, 0)) AS discountAmount " +
           "FROM FeeDue d WHERE d.student.id IN :studentIds GROUP BY d.student.id")
    List<StudentFeeAggregation> aggregateFeesByStudentIds(@org.springframework.data.repository.query.Param("studentIds") List<Long> studentIds);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT d.student.id FROM FeeDue d " +
           "WHERE d.student.id IN :studentIds " +
           "AND d.dueDate IS NOT NULL AND d.dueDate < :today " +
           "AND (COALESCE(d.amount, 0) - COALESCE(d.paidAmount, 0) - COALESCE(d.discountAmount, 0)) > 0")
    List<Long> findOverdueStudentIds(
            @org.springframework.data.repository.query.Param("studentIds") List<Long> studentIds,
            @org.springframework.data.repository.query.Param("today") java.time.LocalDate today);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.amount) AS totalFees, SUM(d.paidAmount) AS amountPaid, SUM(COALESCE(d.discountAmount, 0)) AS discountAmount " +
           "FROM FeeDue d WHERE d.school.id = :schoolId")
    StudentFeeAggregation aggregateSchoolFeeStats(@org.springframework.data.repository.query.Param("schoolId") Long schoolId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.amount) AS totalFees, SUM(d.paidAmount) AS amountPaid, SUM(COALESCE(d.discountAmount, 0)) AS discountAmount " +
           "FROM FeeDue d JOIN d.student s WHERE d.school.id = :schoolId AND s.schoolClass.id IN :classIds")
    StudentFeeAggregation aggregateClassFeeStats(
            @org.springframework.data.repository.query.Param("schoolId") Long schoolId,
            @org.springframework.data.repository.query.Param("classIds") List<Long> classIds);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.amount) AS totalFees, SUM(d.paidAmount) AS amountPaid, SUM(COALESCE(d.discountAmount, 0)) AS discountAmount " +
           "FROM FeeDue d JOIN d.student s WHERE d.school.id = :schoolId AND s.schoolClass.id IN :classIds AND (:sectionId IS NULL OR s.sectionId = :sectionId)")
    StudentFeeAggregation aggregateClassAndSectionFeeStats(
            @org.springframework.data.repository.query.Param("schoolId") Long schoolId,
            @org.springframework.data.repository.query.Param("classIds") List<Long> classIds,
            @org.springframework.data.repository.query.Param("sectionId") Long sectionId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(d.amount) AS totalFees, SUM(d.paidAmount) AS amountPaid, SUM(COALESCE(d.discountAmount, 0)) AS discountAmount " +
           "FROM FeeDue d JOIN d.student s WHERE d.school.id = :schoolId AND (:sectionId IS NULL OR s.sectionId = :sectionId)")
    StudentFeeAggregation aggregateSectionFeeStats(
            @org.springframework.data.repository.query.Param("schoolId") Long schoolId,
            @org.springframework.data.repository.query.Param("sectionId") Long sectionId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(fc.name, 'Other'), COALESCE(SUM(d.amount), 0) FROM FeeDue d LEFT JOIN d.feeCategory fc WHERE d.school.id = :schoolId GROUP BY fc.name")
    List<Object[]> sumFeesByCategory(@org.springframework.data.repository.query.Param("schoolId") Long schoolId);
}
