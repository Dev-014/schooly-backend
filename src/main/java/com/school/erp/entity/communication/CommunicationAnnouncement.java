package com.school.erp.entity.communication;

import com.school.erp.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "communication_announcements")
@Getter
@Setter
public class CommunicationAnnouncement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CommunicationMessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CommunicationImportance importance = CommunicationImportance.INFORMATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CommunicationStatus status = CommunicationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience_type", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CommunicationAudienceType audienceType;

    @Column(name = "audience_criteria")
    @JdbcTypeCode(SqlTypes.JSON)
    private String audienceCriteria; // Store as JSON string, e.g., ["Bangalore", "Mumbai"]

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ElementCollection
    @CollectionTable(name = "communication_announcement_schools", joinColumns = @JoinColumn(name = "announcement_id"))
    @Column(name = "school_id")
    private Set<Long> targetSchoolIds = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
