package com.school.erp.repository.accounting;

import com.school.erp.entity.accounting.Expense;
import com.school.erp.entity.superadmin.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findBySchool(School school);
    List<Expense> findBySchoolAndAcademicYear_Id(School school, Long academicYearId);
    Optional<Expense> findByIdAndSchool(Long id, School school);
    boolean existsByIdAndSchool_Id(Long id, Long schoolId);

    @Query("""
            select e
            from Expense e
            where e.school.id = :schoolId
              and (:academicYearId is null or e.academicYear.id = :academicYearId)
              and (:headId is null or e.expenseHead.id = :headId)
              and (cast(:accountType as string) is null or lower(e.accountType) = lower(cast(:accountType as string)))
              and (:startDate is null or e.date >= :startDate)
              and (:endDate is null or e.date <= :endDate)
              and (:month is null or extract(month from e.date) = :month)
              and (:year is null or extract(year from e.date) = :year)
              and (cast(:paymentMode as string) is null
                   or lower(e.paymentMode) = lower(cast(:paymentMode as string))
                   or (cast(:paymentMode as string) = 'Bank Transfer' and lower(e.paymentMode) = 'bank')
                   or (cast(:paymentMode as string) = 'Card' and lower(e.paymentMode) = 'credit card'))
            order by e.date desc, e.id desc
            """)
    List<Expense> findAllFiltered(
            @Param("schoolId") Long schoolId,
            @Param("academicYearId") Long academicYearId,
            @Param("headId") Long headId,
            @Param("accountType") String accountType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("paymentMode") String paymentMode
    );

    @Query("""
            select e.paymentMode, e.accountType, sum(e.amount)
            from Expense e
            where e.school.id = :schoolId
              and (:academicYearId is null or e.academicYear.id = :academicYearId)
              and (:headId is null or e.expenseHead.id = :headId)
              and (cast(:accountType as string) is null or lower(e.accountType) = lower(cast(:accountType as string)))
              and (:startDate is null or e.date >= :startDate)
              and (:endDate is null or e.date <= :endDate)
              and (:month is null or extract(month from e.date) = :month)
              and (:year is null or extract(year from e.date) = :year)
              and (cast(:paymentMode as string) is null
                   or lower(e.paymentMode) = lower(cast(:paymentMode as string))
                   or (cast(:paymentMode as string) = 'Bank Transfer' and lower(e.paymentMode) = 'bank')
                   or (cast(:paymentMode as string) = 'Card' and lower(e.paymentMode) = 'credit card'))
            group by e.paymentMode, e.accountType
            """)
    List<Object[]> sumFilteredByPaymentMode(
            @Param("schoolId") Long schoolId,
            @Param("academicYearId") Long academicYearId,
            @Param("headId") Long headId,
            @Param("accountType") String accountType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("paymentMode") String paymentMode
    );

    @Query("""
            select coalesce(sum(e.amount), 0)
            from Expense e
            where e.school.id = :schoolId
              and extract(month from e.date) = :month
              and extract(year from e.date) = :year
            """)
    BigDecimal sumAmountBySchoolAndMonthAndYear(
            @Param("schoolId") Long schoolId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
            select cast(extract(month from e.date) as integer), coalesce(sum(e.amount), 0)
            from Expense e
            where e.school.id = :schoolId
              and extract(year from e.date) = :year
            group by extract(month from e.date)
            """)
    List<Object[]> sumMonthlyAmountBySchoolAndYear(
            @Param("schoolId") Long schoolId,
            @Param("year") int year
    );
}
