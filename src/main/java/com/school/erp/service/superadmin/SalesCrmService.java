package com.school.erp.service.superadmin;

import com.school.erp.dto.crm.*;
import com.school.erp.entity.crm.*;
import com.school.erp.repository.crm.*;
import com.school.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesCrmService {

    private final CrmLeadRepository leadRepository;
    private final CrmFollowUpRepository followUpRepository;
    private final CrmDemoRepository demoRepository;
    private final CrmQuotationRepository quotationRepository;
    private final CrmActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CrmDashboardStatsDto getDashboardStats() {
        CrmDashboardStatsDto stats = new CrmDashboardStatsDto();
        long totalLeads = leadRepository.count();
        stats.setTotalLeads(totalLeads);
        
        long newLeads = leadRepository.findAll().stream()
            .filter(l -> l.getPipelineStage() == CrmPipelineStage.NEW)
            .count();
        stats.setNewLeads(newLeads);

        long wonDeals = leadRepository.findAll().stream()
            .filter(l -> l.getPipelineStage() == CrmPipelineStage.WON)
            .count();
        stats.setWonDeals(wonDeals);

        long lostDeals = leadRepository.findAll().stream()
            .filter(l -> l.getPipelineStage() == CrmPipelineStage.LOST)
            .count();
        stats.setLostDeals(lostDeals);
        
        if (totalLeads > 0) {
            stats.setConversionRate((double) wonDeals / totalLeads * 100);
        }

        stats.setTodaysFollowUps(followUpRepository.findAll().stream()
            .filter(f -> f.getScheduledDate().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
            .count());
            
        return stats;
    }

    @Transactional(readOnly = true)
    public List<CrmLeadDto> getAllLeads() {
        return leadRepository.findAll().stream().map(this::mapToLeadDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CrmLeadDto getLeadById(Long id) {
        CrmLead lead = leadRepository.findById(id).orElseThrow(() -> new RuntimeException("Lead not found"));
        return mapToLeadDto(lead);
    }

    @Transactional
    public CrmLeadDto createLead(CreateCrmLeadRequest request) {
        CrmLead lead = new CrmLead();
        lead.setSchoolName(request.getSchoolName());
        lead.setPrincipalName(request.getPrincipalName());
        lead.setCity(request.getCity());
        lead.setBoard(request.getBoard());
        lead.setMobile(request.getMobile());
        lead.setAlternativeMobile(request.getAlternativeMobile());
        lead.setEmail(request.getEmail());
        lead.setAddress(request.getAddress());
        lead.setState(request.getState());
        lead.setPinCode(request.getPinCode());
        lead.setApproxStudentStrength(request.getApproxStudentStrength());
        lead.setTeachers(request.getTeachers());
        lead.setBranches(request.getBranches());
        lead.setCurrentErp(request.getCurrentErp());
        lead.setWebsite(request.getWebsite());
        lead.setExistingProblems(request.getExistingProblems());
        lead.setLeadSource(request.getLeadSource());
        
        lead = leadRepository.save(lead);
        logActivity(lead, null, "Lead Created", "New lead generated from " + request.getLeadSource());
        
        return mapToLeadDto(lead);
    }

    @Transactional
    public void logActivity(CrmLead lead, Long actorId, String activityType, String description) {
        CrmActivityLog log = new CrmActivityLog();
        log.setLead(lead);
        log.setActivityType(activityType);
        log.setDescription(description);
        if (actorId != null) {
            log.setActor(userRepository.findById(actorId).orElse(null));
        }
        activityLogRepository.save(log);
    }

    private CrmLeadDto mapToLeadDto(CrmLead lead) {
        CrmLeadDto dto = new CrmLeadDto();
        dto.setId(lead.getId());
        dto.setSchoolName(lead.getSchoolName());
        dto.setPrincipalName(lead.getPrincipalName());
        dto.setCity(lead.getCity());
        dto.setBoard(lead.getBoard());
        dto.setMobile(lead.getMobile());
        dto.setAlternativeMobile(lead.getAlternativeMobile());
        dto.setEmail(lead.getEmail());
        dto.setAddress(lead.getAddress());
        dto.setState(lead.getState());
        dto.setPinCode(lead.getPinCode());
        dto.setApproxStudentStrength(lead.getApproxStudentStrength());
        dto.setTeachers(lead.getTeachers());
        dto.setBranches(lead.getBranches());
        dto.setCurrentErp(lead.getCurrentErp());
        dto.setWebsite(lead.getWebsite());
        dto.setExistingProblems(lead.getExistingProblems());
        dto.setLeadSource(lead.getLeadSource());
        dto.setPriority(lead.getPriority());
        dto.setExpectedClosingDate(lead.getExpectedClosingDate());
        dto.setLeadRating(lead.getLeadRating());
        dto.setNotes(lead.getNotes());
        dto.setPipelineStage(lead.getPipelineStage());
        dto.setStatus(lead.getStatus());
        dto.setCreatedAt(lead.getCreatedAt());
        dto.setUpdatedAt(lead.getUpdatedAt());
        
        if (lead.getAssignedEmployee() != null) {
            dto.setAssignedEmployeeId(lead.getAssignedEmployee().getId());
            dto.setAssignedEmployeeName(lead.getAssignedEmployee().getName());
        }
        
        if (lead.getConvertedSchool() != null) {
            dto.setConvertedSchoolId(lead.getConvertedSchool().getId());
        }
        
        dto.setLostReason(lead.getLostReason());
        
        dto.setFollowUps(lead.getFollowUps().stream().map(this::mapToFollowUpDto).collect(Collectors.toList()));
        dto.setDemos(lead.getDemos().stream().map(this::mapToDemoDto).collect(Collectors.toList()));
        dto.setQuotations(lead.getQuotations().stream().map(this::mapToQuotationDto).collect(Collectors.toList()));
        dto.setActivityLogs(lead.getActivityLogs().stream().map(this::mapToActivityLogDto).collect(Collectors.toList()));
        
        return dto;
    }

    private CrmFollowUpDto mapToFollowUpDto(CrmFollowUp followUp) {
        CrmFollowUpDto dto = new CrmFollowUpDto();
        dto.setId(followUp.getId());
        dto.setLeadId(followUp.getLead().getId());
        dto.setActionType(followUp.getActionType());
        dto.setScheduledDate(followUp.getScheduledDate());
        dto.setRemarks(followUp.getRemarks());
        dto.setStatus(followUp.getStatus());
        dto.setCreatedAt(followUp.getCreatedAt());
        if (followUp.getExecutive() != null) {
            dto.setExecutiveId(followUp.getExecutive().getId());
            dto.setExecutiveName(followUp.getExecutive().getName());
        }
        return dto;
    }

    private CrmDemoDto mapToDemoDto(CrmDemo demo) {
        CrmDemoDto dto = new CrmDemoDto();
        dto.setId(demo.getId());
        dto.setLeadId(demo.getLead().getId());
        dto.setDemoDate(demo.getDemoDate());
        dto.setMode(demo.getMode());
        dto.setStatus(demo.getStatus());
        dto.setFeedback(demo.getFeedback());
        dto.setRecordingUrl(demo.getRecordingUrl());
        dto.setMeetingNotes(demo.getMeetingNotes());
        dto.setCreatedAt(demo.getCreatedAt());
        if (demo.getDemoBy() != null) {
            dto.setDemoById(demo.getDemoBy().getId());
            dto.setDemoByName(demo.getDemoBy().getName());
        }
        return dto;
    }

    private CrmQuotationDto mapToQuotationDto(CrmQuotation quotation) {
        CrmQuotationDto dto = new CrmQuotationDto();
        dto.setId(quotation.getId());
        dto.setQuotationNumber(quotation.getQuotationNumber());
        dto.setLeadId(quotation.getLead().getId());
        dto.setPlanName(quotation.getPlanName());
        dto.setAmount(quotation.getAmount());
        dto.setDiscount(quotation.getDiscount());
        dto.setGst(quotation.getGst());
        dto.setTotal(quotation.getTotal());
        dto.setExpiryDate(quotation.getExpiryDate());
        dto.setStatus(quotation.getStatus());
        dto.setCreatedAt(quotation.getCreatedAt());
        return dto;
    }

    private CrmActivityLogDto mapToActivityLogDto(CrmActivityLog log) {
        CrmActivityLogDto dto = new CrmActivityLogDto();
        dto.setId(log.getId());
        dto.setLeadId(log.getLead().getId());
        dto.setActivityType(log.getActivityType());
        dto.setDescription(log.getDescription());
        dto.setCreatedAt(log.getCreatedAt());
        if (log.getActor() != null) {
            dto.setActorId(log.getActor().getId());
            dto.setActorName(log.getActor().getName());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<CrmFollowUpDto> getAllFollowUps() {
        return followUpRepository.findAll().stream().map(this::mapToFollowUpDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CrmDemoDto> getAllDemos() {
        return demoRepository.findAll().stream().map(this::mapToDemoDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CrmQuotationDto> getAllQuotations() {
        return quotationRepository.findAll().stream().map(this::mapToQuotationDto).collect(Collectors.toList());
    }

    @Transactional
    public CrmLeadDto updateLeadStage(Long id, UpdateLeadStageRequest request) {
        CrmLead lead = leadRepository.findById(id).orElseThrow(() -> new RuntimeException("Lead not found"));
        CrmPipelineStage oldStage = lead.getPipelineStage();
        lead.setPipelineStage(request.getPipelineStage());
        leadRepository.save(lead);
        logActivity(lead, null, "Stage Updated", "Stage changed from " + oldStage + " to " + request.getPipelineStage());
        return mapToLeadDto(lead);
    }

    @Transactional
    public CrmLeadDto logFollowUp(Long id, LogFollowUpRequest request) {
        CrmLead lead = leadRepository.findById(id).orElseThrow(() -> new RuntimeException("Lead not found"));
        CrmFollowUp followUp = new CrmFollowUp();
        followUp.setLead(lead);
        followUp.setActionType(request.getActionType());
        followUp.setScheduledDate(request.getScheduledDate());
        followUp.setRemarks(request.getRemarks());
        followUp.setStatus(CrmFollowUpStatus.COMPLETED);
        followUpRepository.save(followUp);
        logActivity(lead, null, "Follow-up Logged", "Logged " + request.getActionType() + " follow-up");
        return mapToLeadDto(lead);
    }

    @Transactional
    public CrmLeadDto scheduleDemo(Long id, ScheduleDemoRequest request) {
        CrmLead lead = leadRepository.findById(id).orElseThrow(() -> new RuntimeException("Lead not found"));
        CrmDemo demo = new CrmDemo();
        demo.setLead(lead);
        demo.setDemoDate(request.getDemoDate());
        demo.setMode(request.getMode());
        demo.setMeetingNotes(request.getMeetingNotes());
        demo.setStatus(CrmDemoStatus.SCHEDULED);
        demoRepository.save(demo);
        logActivity(lead, null, "Demo Scheduled", "Scheduled " + request.getMode() + " demo for " + request.getDemoDate());
        return mapToLeadDto(lead);
    }
}
