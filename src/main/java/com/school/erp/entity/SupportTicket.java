package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_code", nullable = false, unique = true)
    private String ticketCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private User creatorUser;

    @Column(name = "portal_source", nullable = false)
    private String portalSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TicketCategory category;

    @Column(name = "priority")
    private String priority = "LOW";

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "description")
    private String description;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "status")
    private String status = "NEW";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id")
    private User assignedEmployee;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}
