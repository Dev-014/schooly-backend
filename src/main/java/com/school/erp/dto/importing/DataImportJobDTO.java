package com.school.erp.dto.importing;

import java.util.List;

public record DataImportJobDTO(
        Long jobId,
        Long schoolId,
        String category,
        String fileName,
        String status,
        Integer totalRecords,
        Integer successfulRecords,
        Integer failedRecords,
        List<DataImportMappingDTO> mappings,
        List<DataImportErrorDTO> errors,
        String createdAt
) {
}
