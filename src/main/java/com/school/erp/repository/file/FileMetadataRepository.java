package com.school.erp.repository.file;

import com.school.erp.entity.file.FileMetadata;
import com.school.erp.enums.file.FileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    Optional<FileMetadata> findByIdAndSchoolIdAndDeletedAtIsNull(UUID id, Long schoolId);

    Optional<FileMetadata> findByIdAndDeletedAtIsNull(UUID id);

    Optional<FileMetadata> findByStorageKey(String storageKey);

    Page<FileMetadata> findBySchoolIdAndStatusAndDeletedAtIsNull(Long schoolId, FileStatus status, Pageable pageable);

    @Query("SELECT f FROM FileMetadata f WHERE f.school.id = :schoolId AND f.deletedAt IS NULL AND (:status IS NULL OR f.status = :status)")
    Page<FileMetadata> searchFiles(@Param("schoolId") Long schoolId, @Param("status") FileStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(f.fileSizeBytes), 0) FROM FileMetadata f WHERE f.school.id = :schoolId AND f.status = 'ACTIVE' AND f.deletedAt IS NULL")
    long calculateTotalUsedBytesBySchoolId(@Param("schoolId") Long schoolId);
}

