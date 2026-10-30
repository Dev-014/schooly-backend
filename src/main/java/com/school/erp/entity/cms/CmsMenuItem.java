package com.school.erp.entity.cms;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cms_menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsMenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_group_id", nullable = false)
    private Long menuGroupId;

    @Column(nullable = false)
    private String title;

    @Column(name = "menu_type")
    private String menuType; // 'page' or 'external'

    private String url;

    @Column(name = "page_id")
    private Long pageId;

    @Builder.Default
    @Column(name = "open_in_new_tab")
    private Boolean openInNewTab = false;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "parent_id")
    private Long parentId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

