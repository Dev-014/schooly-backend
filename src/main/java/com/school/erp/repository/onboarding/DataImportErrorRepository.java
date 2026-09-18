package com.school.erp.repository;

import com.school.erp.entity.DataImportError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataImportErrorRepository extends JpaRepository<DataImportError, Long> {
    List<DataImportError> findByJobIdOrderByErrorIdAsc(Long jobId);
    List<DataImportError> findByJobIdAndResolvedFalseOrderByErrorIdAsc(Long jobId);
}
