package com.school.erp.repository.file;

import com.school.erp.entity.file.FileAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileAuditLogRepository extends JpaRepository<FileAuditLog, Long> {

    List<FileAuditLog> findByFileIdOrderByCreatedAtDesc(UUID fileId);

    Page<FileAuditLog> findBySchoolIdOrderByCreatedAtDesc(Long schoolId, Pageable pageable);
}

