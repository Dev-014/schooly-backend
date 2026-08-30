package com.school.erp.repository;

import com.school.erp.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findBySchoolId(Long schoolId);
    Page<Payment> findBySchoolId(Long schoolId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.school.id = :schoolId AND " +
           "(:search IS NULL OR LOWER(p.student.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.student.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Payment> findBySchoolIdAndSearch(@Param("schoolId") Long schoolId, @Param("search") String search, Pageable pageable);



    @Query("""
            select coalesce(sum(p.amount), 0)
            from Payment p
            where p.school.id = :schoolId
              and extract(month from p.createdAt) = :month
              and extract(year from p.createdAt) = :year
            """)
    BigDecimal sumAmountBySchoolAndMonthAndYear(
            @Param("schoolId") Long schoolId,
            @Param("month") int month,
            @Param("year") int year
    );
}
