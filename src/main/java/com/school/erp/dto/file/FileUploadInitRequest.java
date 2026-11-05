package com.school.erp.dto.file;

import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FileUploadInitRequest(
        @NotBlank(message = "File name is required")
        String fileName,

        @NotBlank(message = "Content type is required")
        String contentType,

        @NotNull(message = "File size is required")
        @Positive(message = "File size must be greater than zero")
        Long fileSize,

        @NotNull(message = "Entity type is required")
        FileEntityType entityType,

        @NotNull(message = "Entity ID is required")
        Long entityId,

        @NotNull(message = "File category is required")
        FileCategory category
) {}

