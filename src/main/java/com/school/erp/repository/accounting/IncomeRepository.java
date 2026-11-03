package com.school.erp.repository.accounting;

import com.school.erp.entity.accounting.Income;
import com.school.erp.entity.superadmin.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findBySchool(School school);
    Optional<Income> findByIdAndSchool(Long id, School school);

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
