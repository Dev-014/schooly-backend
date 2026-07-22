package com.school.erp.dto.importing;

public record DataImportMappingDTO(
        Long id,
        String excelHeader,
        String exampleValue,
        String slateField,
        Integer confidence,
        String status
) {
}
