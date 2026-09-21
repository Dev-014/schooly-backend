package com.school.erp.repository.finance;

import com.school.erp.entity.finance.CollectionPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionPlanItemRepository extends JpaRepository<CollectionPlanItem, Long> {
}
