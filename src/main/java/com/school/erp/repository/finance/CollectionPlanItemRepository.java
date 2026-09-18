package com.school.erp.repository;

import com.school.erp.entity.CollectionPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionPlanItemRepository extends JpaRepository<CollectionPlanItem, Long> {
}
