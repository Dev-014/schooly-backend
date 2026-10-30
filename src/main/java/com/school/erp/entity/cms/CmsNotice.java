package com.school.erp.entity.cms;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "cms_notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private Long schoolId;

    @Column(nullable = false)
    private String title;

    @Column(name = "notice_date")
    private String date;

    @Column(name = "featured_image", columnDefinition = "TEXT")
    private String featuredImage;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String url;

    @Column(name = "seo_title")
    private String seoTitle;

    @Column(name = "seo_keyword")
    private String seoKeyword;

    @Column(name = "seo_description", columnDefinition = "TEXT")
    private String seoDescription;

    @Builder.Default
    private Boolean sidebar = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

