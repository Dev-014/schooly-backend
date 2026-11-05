package com.school.erp.dto.file;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record FileUploadInitResponse(
        UUID fileId,
        String uploadUrl,
        String httpMethod,
        String storageKey,
        Map<String, String> requiredHeaders,
        Instant expiresAt
) {}

