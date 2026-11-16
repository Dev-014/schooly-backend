package com.school.erp.dto.accounting;

import java.util.UUID;

public record AccountingDocumentResponse(
        UUID id,
        String fileName,
        String mimeType,
        Long fileSizeBytes
) {}
