package com.school.erp.repository;

import com.school.erp.entity.FeeInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeInvoiceItemRepository extends JpaRepository<FeeInvoiceItem, Long> {
    List<FeeInvoiceItem> findByFeeInvoiceId(Long feeInvoiceId);
}
