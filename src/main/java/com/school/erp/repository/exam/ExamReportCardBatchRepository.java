package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamReportCardBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamReportCardBatchRepository extends JpaRepository<ExamReportCardBatch, Long> {

    Optional<ExamReportCardBatch> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<ExamReportCardBatch> findFirstBySchoolIdAndStatusOrderByCreatedAtDesc(Long schoolId, String status);

    List<ExamReportCardBatch> findTop5BySchoolIdOrderByCreatedAtDesc(Long schoolId);

    Page<ExamReportCardBatch> findBySchoolIdOrderByCreatedAtDesc(Long schoolId, Pageable pageable);
}
