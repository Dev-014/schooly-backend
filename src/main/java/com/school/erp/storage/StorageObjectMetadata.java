package com.school.erp.storage;

import java.time.Instant;

public record StorageObjectMetadata(
        long sizeBytes,
        String contentType,
        String eTag,
        Instant lastModified
) {}

