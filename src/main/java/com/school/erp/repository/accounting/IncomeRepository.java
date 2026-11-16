package com.school.erp.repository.accounting;

import com.school.erp.entity.accounting.Income;
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
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findBySchool(School school);
    List<Income> findBySchoolAndAcademicYear_Id(School school, Long academicYearId);
    Optional<Income> findByIdAndSchool(Long id, School school);
    boolean existsByIdAndSchool_Id(Long id, Long schoolId);

    @Query("""
            select i
            from Income i
            where i.school.id = :schoolId
              and (:academicYearId is null or i.academicYear.id = :academicYearId)
              and (:headId is null or i.incomeHead.id = :headId)
              and (cast(:accountType as string) is null or lower(i.accountType) = lower(cast(:accountType as string)))
              and (:startDate is null or i.date >= :startDate)
              and (:endDate is null or i.date <= :endDate)
              and (:month is null or extract(month from i.date) = :month)
              and (:year is null or extract(year from i.date) = :year)
              and (cast(:paymentMode as string) is null
                   or lower(i.paymentMode) = lower(cast(:paymentMode as string))
                   or (cast(:paymentMode as string) = 'Bank Transfer' and lower(i.paymentMode) = 'bank')
                   or (cast(:paymentMode as string) = 'Card' and lower(i.paymentMode) = 'credit card'))
            order by i.date desc, i.id desc
            """)
    List<Income> findAllFiltered(
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
            select i.paymentMode, i.accountType, sum(i.amount)
            from Income i
            where i.school.id = :schoolId
              and (:academicYearId is null or i.academicYear.id = :academicYearId)
              and (:headId is null or i.incomeHead.id = :headId)
              and (cast(:accountType as string) is null or lower(i.accountType) = lower(cast(:accountType as string)))
              and (:startDate is null or i.date >= :startDate)
              and (:endDate is null or i.date <= :endDate)
              and (:month is null or extract(month from i.date) = :month)
              and (:year is null or extract(year from i.date) = :year)
              and (cast(:paymentMode as string) is null
                   or lower(i.paymentMode) = lower(cast(:paymentMode as string))
                   or (cast(:paymentMode as string) = 'Bank Transfer' and lower(i.paymentMode) = 'bank')
                   or (cast(:paymentMode as string) = 'Card' and lower(i.paymentMode) = 'credit card'))
            group by i.paymentMode, i.accountType
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
            select coalesce(sum(i.amount), 0)
            from Income i
            where i.school.id = :schoolId
              and extract(month from i.date) = :month
              and extract(year from i.date) = :year
            """)
    BigDecimal sumAmountBySchoolAndMonthAndYear(
            @Param("schoolId") Long schoolId,
            @Param("month") int month,
            @Param("year") int year
    );

    @Query("""
            select cast(extract(month from i.date) as integer), coalesce(sum(i.amount), 0)
            from Income i
            where i.school.id = :schoolId
              and extract(year from i.date) = :year
            group by extract(month from i.date)
            """)
    List<Object[]> sumMonthlyAmountBySchoolAndYear(
            @Param("schoolId") Long schoolId,
            @Param("year") int year
    );
}
