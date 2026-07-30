package com.school.erp.entity.crm;

import com.school.erp.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "crm_demos")
public class CrmDemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id", nullable = false)
    private CrmLead lead;

    @Column(name = "demo_date", nullable = false)
    private LocalDateTime demoDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private CrmDemoMode mode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demo_by_id")
    private User demoBy;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CrmDemoStatus status = CrmDemoStatus.SCHEDULED;

    private String feedback;

    @Column(name = "recording_url")
    private String recordingUrl;

    @Column(name = "meeting_notes")
    private String meetingNotes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
