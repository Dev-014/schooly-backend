package com.school.erp.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsGalleryDto {
    private Long id;
    private Long schoolId;
    private String title;
    private String description;
    private String featuredImage;
    private List<String> galleryImages;
    private String url;
    private Boolean isInternalOnly;
    private String seoTitle;
    private String seoKeyword;
    private String seoDescription;
    private Boolean sidebar;
}

