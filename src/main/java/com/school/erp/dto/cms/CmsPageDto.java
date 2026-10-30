package com.school.erp.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsPageDto {
    private Long id;
    private Long schoolId;
    private String title;
    private String pageType;
    private String url;
    private String featuredImage;
    private String description;
    private String seoTitle;
    private String seoKeyword;
    private String seoDescription;
    private Boolean sidebar;
}

