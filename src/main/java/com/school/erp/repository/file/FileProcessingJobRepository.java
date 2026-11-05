package com.school.erp.repository.file;

import com.school.erp.entity.file.FileProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileProcessingJobRepository extends JpaRepository<FileProcessingJob, Long> {

    List<FileProcessingJob> findByFileIdOrderByCreatedAtDesc(UUID fileId);

    List<FileProcessingJob> findByStatusOrderByCreatedAtAsc(String status);
}

