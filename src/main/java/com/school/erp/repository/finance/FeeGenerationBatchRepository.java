package com.school.erp.repository;

import com.school.erp.entity.FeeGenerationBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeGenerationBatchRepository extends JpaRepository<FeeGenerationBatch, Long> {
}
