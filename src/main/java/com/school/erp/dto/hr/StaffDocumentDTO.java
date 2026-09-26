package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDocumentDTO {
    private Long id;
    private Long staffId;
    private String documentType;
    private String fileName;
    private String fileUrl;
}
