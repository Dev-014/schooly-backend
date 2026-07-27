package com.school.erp.dto.student;

import jakarta.validation.constraints.NotBlank;

public record StudentDocumentRequest(
        @NotBlank(message = "Document name is required")
        String documentName,

        String documentType,

        @NotBlank(message = "File URL is required")
        String fileUrl
) {}
