package com.school.erp.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsBannerDto {
    private Long id;
    private Long schoolId;
    private String fileName;
    private String url;
    private String fileType;
    private String dimensions;
    private String size;
    private String uploadDate;
}

