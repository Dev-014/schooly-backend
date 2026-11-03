package com.school.erp.repository.accounting;

import com.school.erp.entity.accounting.Expense;
import com.school.erp.entity.superadmin.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findBySchool(School school);
    Optional<Expense> findByIdAndSchool(Long id, School school);

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
