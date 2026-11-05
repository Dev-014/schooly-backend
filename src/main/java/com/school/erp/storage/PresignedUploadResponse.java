package com.school.erp.storage;

import java.time.Instant;
import java.util.Map;

public record PresignedUploadResponse(
        String uploadUrl,
        String httpMethod,
        Map<String, String> requiredHeaders,
        Instant expiresAt
) {}

