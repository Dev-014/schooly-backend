package com.school.erp.repository;

import com.school.erp.entity.FeeStructureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeStructureItemRepository extends JpaRepository<FeeStructureItem, Long> {
    List<FeeStructureItem> findByFeeStructureId(Long feeStructureId);
}
