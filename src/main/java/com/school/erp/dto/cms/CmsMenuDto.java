package com.school.erp.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class CmsMenuDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuGroupDto {
        private Long id;
        private Long schoolId;
        private String title;
        private String description;
        private Boolean openInNewTab;
        @Builder.Default
        private List<MenuItemDto> items = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemDto {
        private Long id;
        private Long menuGroupId;
        private String title;
        private String menuType; // 'page' or 'external'
        private String url;
        private Long pageId;
        private Boolean openInNewTab;
        private Integer sortOrder;
        private Long parentId;
        @Builder.Default
        private List<MenuItemDto> children = new ArrayList<>();
    }
}

