package com.school.erp.service.common;

import com.school.erp.dto.common.*;
import com.school.erp.entity.auth.User;
import com.school.erp.entity.common.*;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.auth.UserRepository;
import com.school.erp.repository.common.*;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final TicketCategoryRepository categoryRepository;
    private final TicketHistoryRepository historyRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<SupportTicketDTO> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupportTicketDTO getTicket(Long id) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return mapToDTO(ticket);
    }
    
    @Transactional(readOnly = true)
    public List<TicketHistoryDTO> getTicketHistory(Long ticketId) {
        return historyRepository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(this::mapHistoryToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public TicketDashboardStatsDTO getDashboardStats() {
        long newCount = ticketRepository.countByStatus("NEW");
        long workingCount = ticketRepository.countByStatus("WORKING");
        long waitingCount = ticketRepository.countByStatus("WAITING_FOR_SCHOOL");
        long solvedCount = ticketRepository.countByStatus("SOLVED");
        long closedCount = ticketRepository.countByStatus("CLOSED");
        
        return TicketDashboardStatsDTO.builder()
                .newTickets(newCount)
                .workingTickets(workingCount)
                .solvedToday(solvedCount)
                .closedToday(closedCount)
                .waitingForSchool(waitingCount)
                .build();
    }

    @Transactional
    public SupportTicketDTO createTicket(CreateSupportTicketRequest request, Long schoolId, Long creatorUserId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        TicketCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        SupportTicket ticket = new SupportTicket();
        ticket.setTicketCode("TICK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        ticket.setSchool(school);
        ticket.setCreatorUser(creator);
        ticket.setPortalSource(request.getPortalSource());
        ticket.setCategory(category);
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : "LOW");
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setAttachmentUrl(request.getAttachmentUrl());
        ticket.setStatus("NEW");

        ticket = ticketRepository.save(ticket);
        
        logHistory(ticket, null, "NEW", creator, "Ticket created");

        if (notificationService != null) {
            NotificationPriority priority = "HIGH".equalsIgnoreCase(ticket.getPriority()) || "URGENT".equalsIgnoreCase(ticket.getPriority())
                    ? NotificationPriority.HIGH
                    : NotificationPriority.MEDIUM;
            Map<String, Object> metadata = Map.of(
                    "ticketId", ticket.getId(),
                    "ticketCode", ticket.getTicketCode(),
                    "subject", ticket.getSubject()
            );

            notificationService.broadcastToRole(
                    school.getId(),
                    "SUPER_ADMIN",
                    NotificationType.SUPPORT_TICKET_CREATED,
                    NotificationCategory.SYSTEM,
                    priority,
                    "New Support Ticket (" + ticket.getTicketCode() + ")",
                    school.getName() + " submitted ticket " + ticket.getTicketCode() + ": " + ticket.getSubject(),
                    creator.getId(),
                    ticket.getPortalSource(),
                    creator.getName(),
                    "/support/tickets",
                    "NAVIGATE",
                    "SUPPORT_TICKET",
                    ticket.getId(),
                    metadata
            );

            notificationService.sendDirectNotification(
                    school.getId(),
                    creator.getId(),
                    ticket.getPortalSource() != null ? ticket.getPortalSource() : "ADMIN",
                    NotificationType.SUPPORT_TICKET_CREATED,
                    NotificationCategory.SYSTEM,
                    NotificationPriority.LOW,
                    "Support Ticket Submitted (" + ticket.getTicketCode() + ")",
                    "Your support ticket \"" + ticket.getSubject() + "\" (" + ticket.getTicketCode() + ") has been logged.",
                    creator.getId(),
                    ticket.getPortalSource(),
                    creator.getName(),
                    "/support/tickets",
                    "NAVIGATE",
                    "SUPPORT_TICKET",
                    ticket.getId(),
                    metadata
            );
        }

        return mapToDTO(ticket);
    }

    @Transactional
    public SupportTicketDTO assignTicket(Long ticketId, AssignTicketRequest request, Long actionUserId) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        User assignee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Assignee not found"));
        User actionUser = userRepository.findById(actionUserId).orElse(null);
        
        String oldStatus = ticket.getStatus();
        ticket.setAssignedEmployee(assignee);
        ticket.setStatus("ASSIGNED");
        
        ticket = ticketRepository.save(ticket);
        logHistory(ticket, oldStatus, "ASSIGNED", actionUser, "Assigned to " + assignee.getName());

        if (notificationService != null) {
            Map<String, Object> metadata = Map.of(
                    "ticketId", ticket.getId(),
                    "ticketCode", ticket.getTicketCode(),
                    "status", "ASSIGNED",
                    "assigneeName", assignee.getName()
            );

            notificationService.sendDirectNotification(
                    ticket.getSchool().getId(),
                    assignee.getId(),
                    "SUPER_ADMIN",
                    NotificationType.SUPPORT_TICKET_UPDATED,
                    NotificationCategory.SYSTEM,
                    NotificationPriority.HIGH,
                    "Support Ticket Assigned (" + ticket.getTicketCode() + ")",
                    "Ticket " + ticket.getTicketCode() + " (\"" + ticket.getSubject() + "\") has been assigned to you.",
                    actionUser != null ? actionUser.getId() : null,
                    "SUPER_ADMIN",
                    actionUser != null ? actionUser.getName() : "System",
                    "/support/tickets",
                    "NAVIGATE",
                    "SUPPORT_TICKET",
                    ticket.getId(),
                    metadata
            );

            if (ticket.getCreatorUser() != null && !ticket.getCreatorUser().getId().equals(assignee.getId())) {
                notificationService.sendDirectNotification(
                        ticket.getSchool().getId(),
                        ticket.getCreatorUser().getId(),
                        ticket.getPortalSource() != null ? ticket.getPortalSource() : "ADMIN",
                        NotificationType.SUPPORT_TICKET_UPDATED,
                        NotificationCategory.SYSTEM,
                        NotificationPriority.MEDIUM,
                        "Ticket Assigned (" + ticket.getTicketCode() + ")",
                        "Your support ticket " + ticket.getTicketCode() + " has been assigned to " + assignee.getName() + ".",
                        actionUser != null ? actionUser.getId() : null,
                        "SUPER_ADMIN",
                        actionUser != null ? actionUser.getName() : "System",
                        "/support/tickets",
                        "NAVIGATE",
                        "SUPPORT_TICKET",
                        ticket.getId(),
                        metadata
                );
            }
        }
        
        return mapToDTO(ticket);
    }

    @Transactional
    public SupportTicketDTO updateStatus(Long ticketId, UpdateTicketStatusRequest request, Long actionUserId) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        User actionUser = userRepository.findById(actionUserId).orElse(null);
        
        String oldStatus = ticket.getStatus();
        String newStatus = request.getStatus();
        
        ticket.setStatus(newStatus);
        if ("CLOSED".equals(newStatus)) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        
        ticket = ticketRepository.save(ticket);
        logHistory(ticket, oldStatus, newStatus, actionUser, request.getRemark());

        if (notificationService != null && ticket.getCreatorUser() != null) {
            notificationService.sendDirectNotification(
                    ticket.getSchool().getId(),
                    ticket.getCreatorUser().getId(),
                    ticket.getPortalSource() != null ? ticket.getPortalSource() : "ADMIN",
                    NotificationType.SUPPORT_TICKET_UPDATED,
                    NotificationCategory.SYSTEM,
                    NotificationPriority.MEDIUM,
                    "Support Ticket " + newStatus + " (" + ticket.getTicketCode() + ")",
                    "Your ticket \"" + ticket.getSubject() + "\" (" + ticket.getTicketCode() + ") is now " + newStatus
                            + (request.getRemark() != null && !request.getRemark().isBlank() ? ". Remark: " + request.getRemark() : "."),
                    actionUser != null ? actionUser.getId() : null,
                    "SUPER_ADMIN",
                    actionUser != null ? actionUser.getName() : "System",
                    "/support/tickets",
                    "NAVIGATE",
                    "SUPPORT_TICKET",
                    ticket.getId(),
                    Map.of(
                            "ticketId", ticket.getId(),
                            "ticketCode", ticket.getTicketCode(),
                            "status", newStatus
                    )
            );
        }
        
        return mapToDTO(ticket);
    }

    private void logHistory(SupportTicket ticket, String oldStatus, String newStatus, User employee, String remark) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setEmployee(employee);
        history.setRemark(remark);
        historyRepository.save(history);
    }

    private SupportTicketDTO mapToDTO(SupportTicket entity) {
        return SupportTicketDTO.builder()
                .id(entity.getId())
                .ticketCode(entity.getTicketCode())
                .schoolId(entity.getSchool().getId())
                .schoolName(entity.getSchool().getName())
                .creatorUserId(entity.getCreatorUser().getId())
                .creatorName(entity.getCreatorUser().getName())
                .portalSource(entity.getPortalSource())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .priority(entity.getPriority())
                .subject(entity.getSubject())
                .description(entity.getDescription())
                .attachmentUrl(entity.getAttachmentUrl())
                .status(entity.getStatus())
                .assignedEmployeeId(entity.getAssignedEmployee() != null ? entity.getAssignedEmployee().getId() : null)
                .assignedEmployeeName(entity.getAssignedEmployee() != null ? entity.getAssignedEmployee().getName() : null)
                .createdAt(entity.getCreatedAt())
                .closedAt(entity.getClosedAt())
                .build();
    }
    
    private TicketHistoryDTO mapHistoryToDTO(TicketHistory entity) {
        return TicketHistoryDTO.builder()
                .id(entity.getId())
                .ticketId(entity.getTicket().getId())
                .oldStatus(entity.getOldStatus())
                .newStatus(entity.getNewStatus())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getName() : null)
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
