package com.school.erp.repository;

import com.school.erp.entity.FeePaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeePaymentItemRepository extends JpaRepository<FeePaymentItem, Long> {
    List<FeePaymentItem> findByPaymentId(Long paymentId);
}
