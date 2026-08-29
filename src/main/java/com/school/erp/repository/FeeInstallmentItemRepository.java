package com.school.erp.repository;

import com.school.erp.entity.FeeInstallmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeInstallmentItemRepository extends JpaRepository<FeeInstallmentItem, Long> {
    List<FeeInstallmentItem> findByFeeInstallmentId(Long installmentId);
}
