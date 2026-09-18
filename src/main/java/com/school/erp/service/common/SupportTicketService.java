package com.school.erp.service;

import com.school.erp.dto.*;
import com.school.erp.entity.*;
import com.school.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
