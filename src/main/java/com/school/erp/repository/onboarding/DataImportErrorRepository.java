package com.school.erp.repository.onboarding;

import com.school.erp.entity.onboarding.DataImportError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataImportErrorRepository extends JpaRepository<DataImportError, Long> {
    List<DataImportError> findByJobIdOrderByErrorIdAsc(Long jobId);
    List<DataImportError> findByJobIdAndResolvedFalseOrderByErrorIdAsc(Long jobId);
}
