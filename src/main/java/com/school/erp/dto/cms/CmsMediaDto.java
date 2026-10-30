package com.school.erp.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsMediaDto {
    private Long id;
    private Long schoolId;
    private String fileName;
    private String fileType;
    private String url;
    private String size;
    private String dimensions;
    private String uploadDate;
    private String youtubeUrl;
}

