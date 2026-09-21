package com.school.erp.repository.finance;

import com.school.erp.entity.finance.FeeGenerationBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeGenerationBatchRepository extends JpaRepository<FeeGenerationBatch, Long> {
}
