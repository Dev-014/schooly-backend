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
@Table(name = "cms_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmsMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type")
    private String fileType; // image, video, document, audio, etc.

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    private String size;

    private String dimensions;

    @Column(name = "upload_date")
    private String uploadDate;

    @Column(name = "youtube_url", columnDefinition = "TEXT")
    private String youtubeUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

