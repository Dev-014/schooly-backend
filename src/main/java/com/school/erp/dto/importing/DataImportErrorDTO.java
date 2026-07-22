package com.school.erp.dto.importing;

public record DataImportErrorDTO(
        Long errorId,
        String rowIndex,
        String category,
        String fieldName,
        String errorMessage,
        String currentValue,
        Boolean resolved
) {
}
