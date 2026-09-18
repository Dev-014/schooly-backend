package com.school.erp.repository;

import com.school.erp.entity.FeeInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, Long> {

    List<FeeInvoice> findBySchoolId(Long schoolId);

    List<FeeInvoice> findBySchoolIdAndStudentId(Long schoolId, Long studentId);

    Optional<FeeInvoice> findByIdAndSchoolId(Long id, Long schoolId);
}
