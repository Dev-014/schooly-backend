package com.school.erp.controller.superadmin;

import com.school.erp.dto.crm.CreateCrmLeadRequest;
import com.school.erp.dto.crm.CrmDashboardStatsDto;
import com.school.erp.dto.crm.CrmLeadDto;
import com.school.erp.dto.crm.CrmFollowUpDto;
import com.school.erp.dto.crm.CrmDemoDto;
import com.school.erp.dto.crm.CrmQuotationDto;
import com.school.erp.dto.crm.UpdateLeadStageRequest;
import com.school.erp.dto.crm.LogFollowUpRequest;
import com.school.erp.dto.crm.ScheduleDemoRequest;
import com.school.erp.service.superadmin.SalesCrmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/crm")
@RequiredArgsConstructor
public class SalesCrmController {

    private final SalesCrmService crmService;

    @GetMapping("/dashboard")
    public ResponseEntity<CrmDashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(crmService.getDashboardStats());
    }

    @GetMapping("/leads")
    public ResponseEntity<List<CrmLeadDto>> getAllLeads() {
        return ResponseEntity.ok(crmService.getAllLeads());
    }

    @GetMapping("/leads/{id}")
    public ResponseEntity<CrmLeadDto> getLeadById(@PathVariable Long id) {
        return ResponseEntity.ok(crmService.getLeadById(id));
    }

    @PostMapping("/leads")
    public ResponseEntity<CrmLeadDto> createLead(@RequestBody CreateCrmLeadRequest request) {
        return ResponseEntity.ok(crmService.createLead(request));
    }

    @GetMapping("/follow-ups")
    public ResponseEntity<List<CrmFollowUpDto>> getAllFollowUps() {
        return ResponseEntity.ok(crmService.getAllFollowUps());
    }

    @GetMapping("/demos")
    public ResponseEntity<List<CrmDemoDto>> getAllDemos() {
        return ResponseEntity.ok(crmService.getAllDemos());
    }

    @GetMapping("/quotations")
    public ResponseEntity<List<CrmQuotationDto>> getAllQuotations() {
        return ResponseEntity.ok(crmService.getAllQuotations());
    }

    @PutMapping("/leads/{id}/stage")
    public ResponseEntity<CrmLeadDto> updateLeadStage(@PathVariable Long id, @RequestBody UpdateLeadStageRequest request) {
        return ResponseEntity.ok(crmService.updateLeadStage(id, request));
    }

    @PostMapping("/leads/{id}/follow-ups")
    public ResponseEntity<CrmLeadDto> logFollowUp(@PathVariable Long id, @RequestBody LogFollowUpRequest request) {
        return ResponseEntity.ok(crmService.logFollowUp(id, request));
    }

    @PostMapping("/leads/{id}/demos")
    public ResponseEntity<CrmLeadDto> scheduleDemo(@PathVariable Long id, @RequestBody ScheduleDemoRequest request) {
        return ResponseEntity.ok(crmService.scheduleDemo(id, request));
    }
}
