package com.school.erp.repository.file;

import com.school.erp.entity.file.FileAttachment;
import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    List<FileAttachment> findBySchoolIdAndEntityTypeAndEntityIdOrderByDisplayOrderAscCreatedAtDesc(
            Long schoolId, FileEntityType entityType, Long entityId);

    List<FileAttachment> findBySchoolIdAndEntityTypeAndEntityIdAndCategoryOrderByDisplayOrderAsc(
            Long schoolId, FileEntityType entityType, Long entityId, FileCategory category);

    Optional<FileAttachment> findByFileId(UUID fileId);

    List<FileAttachment> findAllByFileId(UUID fileId);

    void deleteByFileId(UUID fileId);
}

