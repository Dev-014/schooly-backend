package com.school.erp.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsGalleryPhotoDto {
    private Long id;
    private String url;
    private String caption;
}
