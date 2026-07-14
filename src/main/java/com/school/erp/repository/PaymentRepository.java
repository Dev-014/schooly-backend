package com.school.erp.repository;

import com.school.erp.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findBySchoolId(Long schoolId);

    List<Payment> findBySchoolIdAndInvoiceId(Long schoolId, Long invoiceId);

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
