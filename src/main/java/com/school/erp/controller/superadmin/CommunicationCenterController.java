package com.school.erp.controller.superadmin;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.communication.*;
import com.school.erp.service.superadmin.CommunicationCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/super-admin/communication", "/api/v1/super-admin/communication"})
@RequiredArgsConstructor
public class CommunicationCenterController {

    private final CommunicationCenterService service;

    @PostMapping("/announcements")
    public ResponseEntity<ApiResponse<CommunicationAnnouncementDTO>> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request) {
        // Hardcoding user ID 1L for MVP since Spring Security UserPrincipal wasn't found
        CommunicationAnnouncementDTO dto = service.createAnnouncement(request, 1L);
        return ResponseEntity.ok(ApiResponse.success(dto, "Announcement created"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<CommunicationAnnouncementDTO>>> getHistory(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.getHistory(pageable), "Fetched history"));
    }

    @GetMapping("/scheduled")
    public ResponseEntity<ApiResponse<Page<CommunicationAnnouncementDTO>>> getScheduled(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.getScheduled(pageable), "Fetched scheduled"));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<CommunicationDashboardStatsDTO>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(service.getDashboardStats(), "Fetched stats"));
    }

    @PostMapping("/templates")
    public ResponseEntity<ApiResponse<CommunicationTemplateDTO>> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.createTemplate(request), "Template created"));
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<Page<CommunicationTemplateDTO>>> getTemplates(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.getTemplates(pageable), "Fetched templates"));
    }
}
