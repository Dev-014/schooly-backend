package com.school.erp.controller;

import com.school.erp.dto.*;
import com.school.erp.service.SupportTicketService;
import com.school.erp.service.TicketCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService ticketService;
    private final TicketCategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<SupportTicketDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupportTicketDTO> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicket(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TicketHistoryDTO>> getTicketHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketHistory(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<TicketDashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(ticketService.getDashboardStats());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<TicketCategoryDTO>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    // Usually schoolId/userId comes from Auth Session, keeping it simple via path for MVP mock
    @PostMapping("/school/{schoolId}/user/{userId}")
    public ResponseEntity<SupportTicketDTO> createTicket(
            @PathVariable Long schoolId,
            @PathVariable Long userId,
            @Valid @RequestBody CreateSupportTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request, schoolId, userId));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<SupportTicketDTO> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request,
            @RequestParam(required = false, defaultValue = "1") Long actionUserId) {
        return ResponseEntity.ok(ticketService.assignTicket(id, request, actionUserId));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<SupportTicketDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            @RequestParam(required = false, defaultValue = "1") Long actionUserId) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request, actionUserId));
    }
}
