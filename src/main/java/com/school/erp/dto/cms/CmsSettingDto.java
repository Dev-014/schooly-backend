package com.school.erp.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsSettingDto {

    private Long id;
    private Long schoolId;

    // School Details
    private String websiteLogo;
    private String favIcon;
    private String contactEmail;
    private String complainEmail;
    private String footerText;
    @Builder.Default
    private Boolean schoolAppEnabled = true;
    @Builder.Default
    private Boolean frontWebsiteEnabled = true;
    private String schoolTourVideoUrl;
    private String appDownloadLink;
    private String iosAppLink;
    private String themePrimaryColor;
    private String themeStandardColor;
    private String googleAnalytics;
    private String welcomePopupTitle;
    private String welcomePopupImage;

    // Social Media Links
    private String facebookUrl;
    private String instagramUrl;
    private String whatsappUrl;
    private String xUrl;
    private String linkedinUrl;
    private String youtubeUrl;

    // About School
    private String aboutTitle;
    private String aboutDescription;
    private String aboutImage;

    // Our Mission
    private String missionTitle;
    private String missionDescription;
    private String missionImage;

    // Our Vision
    private String visionTitle;
    private String visionDescription;
    private String visionImage;

    // SEO Link
    private String metaDescription;
    private String metaTitle;
    private String metaKeywords;
    private String ogTitle;
    private String ogDescription;
    private String ogImageUrl;
    private String ogUrl;
    private String ogType;

    // Selected Theme
    @Builder.Default
    private Integer activeThemeId = 1;
}

