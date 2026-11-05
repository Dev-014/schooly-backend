package com.school.erp.dto.file;

import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileEntityType;
import com.school.erp.enums.file.FileStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileResponseDto(
        UUID id,
        Long schoolId,
        String originalFileName,
        String mimeType,
        String fileExtension,
        Long fileSizeBytes,
        FileStatus status,
        String storageKey,
        FileCategory category,
        FileEntityType entityType,
        Long entityId,
        Integer displayOrder,
        Long uploadedByUserId,
        LocalDateTime createdAt
) {}

