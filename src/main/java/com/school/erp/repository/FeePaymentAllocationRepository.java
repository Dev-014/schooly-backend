package com.school.erp.repository;

import com.school.erp.entity.FeePaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeePaymentAllocationRepository extends JpaRepository<FeePaymentAllocation, Long> {
    List<FeePaymentAllocation> findByPaymentId(Long paymentId);
    List<FeePaymentAllocation> findByFeeDueId(Long feeDueId);
}
