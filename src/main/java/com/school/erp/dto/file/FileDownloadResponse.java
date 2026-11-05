package com.school.erp.dto.file;

import java.time.Instant;
import java.util.UUID;

public record FileDownloadResponse(
        UUID fileId,
        String downloadUrl,
        String originalFileName,
        String mimeType,
        Long fileSizeBytes,
        boolean isInline,
        Instant expiresAt
) {}

