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
@Table(name = "cms_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "website_logo", columnDefinition = "TEXT")
    private String websiteLogo;

    @Column(name = "fav_icon", columnDefinition = "TEXT")
    private String favIcon;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "complain_email")
    private String complainEmail;

    @Column(name = "footer_text", columnDefinition = "TEXT")
    private String footerText;

    @Column(name = "school_app_enabled")
    @Builder.Default
    private Boolean schoolAppEnabled = true;

    @Column(name = "front_website_enabled")
    @Builder.Default
    private Boolean frontWebsiteEnabled = true;

    @Column(name = "school_tour_video_url", columnDefinition = "TEXT")
    private String schoolTourVideoUrl;

    @Column(name = "app_download_link", columnDefinition = "TEXT")
    private String appDownloadLink;

    @Column(name = "ios_app_link", columnDefinition = "TEXT")
    private String iosAppLink;

    @Column(name = "theme_primary_color")
    private String themePrimaryColor;

    @Column(name = "theme_standard_color")
    private String themeStandardColor;

    @Column(name = "google_analytics", columnDefinition = "TEXT")
    private String googleAnalytics;

    @Column(name = "welcome_popup_title")
    private String welcomePopupTitle;

    @Column(name = "welcome_popup_image", columnDefinition = "TEXT")
    private String welcomePopupImage;

    // Social Media Links
    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "whatsapp_url")
    private String whatsappUrl;

    @Column(name = "x_url")
    private String xUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "youtube_url")
    private String youtubeUrl;

    // About School
    @Column(name = "about_title")
    private String aboutTitle;

    @Column(name = "about_description", columnDefinition = "TEXT")
    private String aboutDescription;

    @Column(name = "about_image", columnDefinition = "TEXT")
    private String aboutImage;

    // Our Mission
    @Column(name = "mission_title")
    private String missionTitle;

    @Column(name = "mission_description", columnDefinition = "TEXT")
    private String missionDescription;

    @Column(name = "mission_image", columnDefinition = "TEXT")
    private String missionImage;

    // Our Vision
    @Column(name = "vision_title")
    private String visionTitle;

    @Column(name = "vision_description", columnDefinition = "TEXT")
    private String visionDescription;

    @Column(name = "vision_image", columnDefinition = "TEXT")
    private String visionImage;

    // SEO Link
    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_keywords")
    private String metaKeywords;

    @Column(name = "og_title")
    private String ogTitle;

    @Column(name = "og_description", columnDefinition = "TEXT")
    private String ogDescription;

    @Column(name = "og_image_url", columnDefinition = "TEXT")
    private String ogImageUrl;

    @Column(name = "og_url")
    private String ogUrl;

    @Column(name = "og_type")
    private String ogType;

    // Theme Selection
    @Column(name = "active_theme_id")
    @Builder.Default
    private Integer activeThemeId = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

