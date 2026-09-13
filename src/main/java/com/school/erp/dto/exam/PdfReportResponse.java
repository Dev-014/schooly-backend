package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfReportResponse {
    private String fileUrl;
    private String fileName;
    private String reportType;
    private int totalRecords;
    private LocalDateTime generatedAt;
}
