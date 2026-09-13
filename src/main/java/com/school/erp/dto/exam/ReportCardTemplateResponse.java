package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardTemplateResponse {
    private String id;
    private String name;
    private String description;
    private String previewUrl;
    private boolean isDefault;
}
