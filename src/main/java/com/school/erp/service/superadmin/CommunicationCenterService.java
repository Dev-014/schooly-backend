package com.school.erp.service.superadmin;

import com.school.erp.dto.communication.*;
import com.school.erp.entity.School;
import com.school.erp.entity.User;
import com.school.erp.entity.communication.*;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.repository.communication.CommunicationAnnouncementRepository;
import com.school.erp.repository.communication.CommunicationDeliveryRepository;
import com.school.erp.repository.communication.CommunicationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunicationCenterService {

    private final CommunicationAnnouncementRepository announcementRepo;
    private final CommunicationDeliveryRepository deliveryRepo;
    private final CommunicationTemplateRepository templateRepo;
    private final SchoolRepository schoolRepo;
    private final UserRepository userRepo;

    @Transactional
    public CommunicationAnnouncementDTO createAnnouncement(CreateAnnouncementRequest request, Long userId) {
        User creator = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CommunicationAnnouncement announcement = new CommunicationAnnouncement();
        announcement.setSubject(request.getSubject());
        announcement.setMessage(request.getMessage());
        announcement.setMessageType(request.getMessageType());
        announcement.setImportance(request.getImportance());
        announcement.setAudienceType(request.getAudienceType());
        
        if (request.getAudienceCriteria() != null) {
            announcement.setAudienceCriteria(String.join(",", request.getAudienceCriteria()));
        }

        if (request.getSaveAsDraft()) {
            announcement.setStatus(CommunicationStatus.DRAFT);
        } else if (request.getScheduleForLater() && request.getScheduledAt() != null) {
            announcement.setStatus(CommunicationStatus.SCHEDULED);
            announcement.setScheduledAt(request.getScheduledAt());
        } else {
            announcement.setStatus(CommunicationStatus.SENT);
        }

        announcement.setCreatedBy(creator);
        
        // Resolve Target Schools
        Set<School> targetSchools = resolveTargetSchools(request);
        announcement.setTargetSchoolIds(targetSchools.stream().map(School::getId).collect(Collectors.toSet()));

        announcement = announcementRepo.save(announcement);

        // Generate deliveries synchronously for MVP, but easily adaptable to @Async if needed
        if (announcement.getStatus() == CommunicationStatus.SENT) {
            generateDeliveries(announcement, targetSchools, request.getDeliveryChannels());
        }

        return mapToDTO(announcement);
    }

    private Set<School> resolveTargetSchools(CreateAnnouncementRequest request) {
        List<School> allSchools = schoolRepo.findAll();
        Set<School> targeted = new HashSet<>();

        switch (request.getAudienceType()) {
            case ALL_SCHOOLS:
                targeted.addAll(allSchools);
                break;
            case SELECTED_SCHOOLS:
                if (request.getSpecificSchoolIds() != null) {
                    targeted.addAll(allSchools.stream()
                            .filter(s -> request.getSpecificSchoolIds().contains(s.getId()))
                            .collect(Collectors.toList()));
                }
                break;
            case INACTIVE_SCHOOLS:
                targeted.addAll(allSchools.stream()
                        .filter(s -> !"ACTIVE".equalsIgnoreCase(s.getStatus()))
                        .collect(Collectors.toList()));
                break;
            // Other cases can be easily implemented with metadata filters or specific fields
            default:
                targeted.addAll(allSchools);
                break;
        }
        return targeted;
    }

    private void generateDeliveries(CommunicationAnnouncement announcement, Set<School> schools, Set<CommunicationChannel> channels) {
        List<CommunicationDelivery> deliveries = new ArrayList<>();
        
        for (School school : schools) {
            for (CommunicationChannel channel : channels) {
                CommunicationDelivery delivery = new CommunicationDelivery();
                delivery.setAnnouncement(announcement);
                delivery.setSchool(school);
                delivery.setDeliveryChannel(channel);
                delivery.setStatus(CommunicationDeliveryStatus.DELIVERED); // Mock immediate delivery for Portal/Mobile
                delivery.setDeliveredAt(LocalDateTime.now());
                deliveries.add(delivery);
            }
        }
        
        deliveryRepo.saveAll(deliveries);
    }

    @Transactional(readOnly = true)
    public Page<CommunicationAnnouncementDTO> getHistory(Pageable pageable) {
        return announcementRepo.findByStatus(CommunicationStatus.SENT, pageable)
                .map(this::mapToDTOWithStats);
    }

    @Transactional(readOnly = true)
    public Page<CommunicationAnnouncementDTO> getScheduled(Pageable pageable) {
        return announcementRepo.findByStatus(CommunicationStatus.SCHEDULED, pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public CommunicationTemplateDTO createTemplate(CreateTemplateRequest request) {
        CommunicationTemplate t = new CommunicationTemplate();
        t.setTemplateName(request.getTemplateName());
        t.setCategory(request.getCategory());
        t.setMessage(request.getMessage());
        t.setIsActive(true);
        t = templateRepo.save(t);
        return mapTemplateToDTO(t);
    }

    @Transactional(readOnly = true)
    public Page<CommunicationTemplateDTO> getTemplates(Pageable pageable) {
        return templateRepo.findByIsActiveTrue(pageable).map(this::mapTemplateToDTO);
    }

    @Transactional(readOnly = true)
    public CommunicationDashboardStatsDTO getDashboardStats() {
        CommunicationDashboardStatsDTO stats = new CommunicationDashboardStatsDTO();
        
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        
        // Very basic mock stats for MVP. Real stats would require more complex queries.
        stats.setMessagesSentToday(deliveryRepo.countByStatusAndCreatedAtAfter(CommunicationDeliveryStatus.DELIVERED, startOfDay));
        stats.setScheduledMessages(announcementRepo.findByStatus(CommunicationStatus.SCHEDULED, Pageable.unpaged()).getTotalElements());
        stats.setDeliveredToday(deliveryRepo.countByStatusAndCreatedAtAfter(CommunicationDeliveryStatus.DELIVERED, startOfDay));
        stats.setReadToday(deliveryRepo.countByStatusAndCreatedAtAfter(CommunicationDeliveryStatus.READ, startOfDay));
        stats.setUnreadToday(stats.getDeliveredToday() - stats.getReadToday());
        stats.setFailedToday(deliveryRepo.countByStatusAndCreatedAtAfter(CommunicationDeliveryStatus.FAILED, startOfDay));
        
        return stats;
    }

    private CommunicationAnnouncementDTO mapToDTO(CommunicationAnnouncement entity) {
        CommunicationAnnouncementDTO dto = new CommunicationAnnouncementDTO();
        dto.setId(entity.getId());
        dto.setSubject(entity.getSubject());
        dto.setMessage(entity.getMessage());
        dto.setMessageType(entity.getMessageType());
        dto.setImportance(entity.getImportance());
        dto.setStatus(entity.getStatus());
        dto.setAudienceType(entity.getAudienceType());
        
        if (entity.getAudienceCriteria() != null) {
            dto.setAudienceCriteria(Arrays.asList(entity.getAudienceCriteria().split(",")));
        }
        
        dto.setScheduledAt(entity.getScheduledAt());
        if (entity.getCreatedBy() != null) {
            dto.setCreatedByUserId(entity.getCreatedBy().getId());
            dto.setCreatedByUserName(entity.getCreatedBy().getName());
        }
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private CommunicationAnnouncementDTO mapToDTOWithStats(CommunicationAnnouncement entity) {
        CommunicationAnnouncementDTO dto = mapToDTO(entity);
        // Simple aggregate logic. In production, use DB aggregation query.
        Page<CommunicationDelivery> deliveries = deliveryRepo.findByAnnouncementId(entity.getId(), Pageable.unpaged());
        dto.setTotalDeliveries(deliveries.getTotalElements());
        
        long delivered = 0;
        long read = 0;
        long failed = 0;
        
        for (CommunicationDelivery d : deliveries.getContent()) {
            if (d.getStatus() == CommunicationDeliveryStatus.DELIVERED) delivered++;
            if (d.getStatus() == CommunicationDeliveryStatus.READ) read++;
            if (d.getStatus() == CommunicationDeliveryStatus.FAILED) failed++;
        }
        
        dto.setDeliveredCount(delivered + read);
        dto.setReadCount(read);
        dto.setFailedCount(failed);
        
        return dto;
    }

    private CommunicationTemplateDTO mapTemplateToDTO(CommunicationTemplate entity) {
        CommunicationTemplateDTO dto = new CommunicationTemplateDTO();
        dto.setId(entity.getId());
        dto.setTemplateName(entity.getTemplateName());
        dto.setCategory(entity.getCategory());
        dto.setMessage(entity.getMessage());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
