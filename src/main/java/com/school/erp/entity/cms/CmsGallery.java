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
@Table(name = "cms_galleries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsGallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private Long schoolId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "featured_image", columnDefinition = "TEXT")
    private String featuredImage;

    @Column(name = "gallery_images", columnDefinition = "TEXT")
    private String galleryImages; // JSON or comma-separated list of image URLs

    private String url;

    @Builder.Default
    @Column(name = "is_internal_only")
    private Boolean isInternalOnly = false;

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

