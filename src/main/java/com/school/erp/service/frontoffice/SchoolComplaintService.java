package com.school.erp.service.frontoffice;

import com.school.erp.dto.frontoffice.ComplaintRequest;
import com.school.erp.dto.frontoffice.ComplaintResponse;
import com.school.erp.dto.frontoffice.ComplaintStatsResponse;
import com.school.erp.entity.frontoffice.SchoolComplaint;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.frontoffice.SchoolComplaintRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SchoolComplaintService {

    private final AuthContextService authContextService;
    private final SchoolComplaintRepository schoolComplaintRepository;
    private final SchoolRepository schoolRepository;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> filterComplaints(
            Long rawSchoolId,
            String search,
            String complaintType,
            String status,
            String scope,
            Pageable pageable) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanType = (complaintType != null && !complaintType.isBlank() && !complaintType.equalsIgnoreCase("all"))
                ? complaintType.trim()
                : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim()
                : null;

        Long filterUserId = null;
        if ("own".equalsIgnoreCase(scope)) {
            com.school.erp.security.AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
            if (currentUser != null && currentUser.userId() != null) {
                filterUserId = currentUser.userId();
            }
        }

        Page<SchoolComplaint> page = schoolComplaintRepository.filterComplaints(
                schoolId, cleanSearch, cleanType, cleanStatus, filterUserId, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        SchoolComplaint complaint = schoolComplaintRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        return mapToResponse(complaint);
    }

    @Transactional
    public ComplaintResponse createComplaint(Long rawSchoolId, ComplaintRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        com.school.erp.security.AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        Long creatorId = request.getCreatedByUserId() != null ? request.getCreatedByUserId()
                : (currentUser != null ? currentUser.userId() : null);
        String creatorRole = request.getCreatedByRole() != null ? request.getCreatedByRole()
                : (currentUser != null && currentUser.role() != null ? currentUser.role().name() : null);

        SchoolComplaint complaint = new SchoolComplaint();
        complaint.setSchool(school);
        complaint.setComplainantName(request.getComplainantName());
        complaint.setMobileNumber(request.getMobileNumber());
        complaint.setComplaintType(request.getComplaintType());
        complaint.setDescription(request.getDescription());
        complaint.setAssignedBy(request.getAssignedBy());
        complaint.setActionTaken(request.getActionTaken());
        complaint.setStatus(
                request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "Open");
        complaint.setComplaintDate(request.getComplaintDate() != null ? request.getComplaintDate() : LocalDate.now());
        complaint.setCreatedByUserId(creatorId);
        complaint.setCreatedByRole(creatorRole);
        complaint.setSourceScope(
                request.getSourceScope() != null && !request.getSourceScope().isBlank() ? request.getSourceScope()
                        : "SCHOOL");
        complaint.setPriority(
                request.getPriority() != null && !request.getPriority().isBlank() ? request.getPriority() : "NORMAL");

        if (request.getComplaintCode() != null && !request.getComplaintCode().isBlank()) {
            complaint.setComplaintCode(request.getComplaintCode());
        }

        SchoolComplaint saved = schoolComplaintRepository.save(complaint);
        if (saved.getComplaintCode() == null || saved.getComplaintCode().isBlank()) {
            saved.setComplaintCode(String.format("CMP-%05d", saved.getId()));
            saved = schoolComplaintRepository.save(saved);
        }

        if (notificationService != null) {
            notificationService.notifySchoolAdmins(
                    schoolId,
                    NotificationType.COMPLAINT_FILED,
                    NotificationPriority.HIGH,
                    "New Complaint Filed (" + saved.getComplaintType() + ")",
                    saved.getComplainantName() + " filed a " + saved.getComplaintType() + " complaint: \""
                            + saved.getDescription() + "\"",
                    "/front-office/complaints",
                    "Review Complaint",
                    "SCHOOL_COMPLAINT",
                    String.valueOf(saved.getId()),
                    Map.of(
                            "complaintId", saved.getId(),
                            "complainantName", saved.getComplainantName(),
                            "complaintType", saved.getComplaintType()));
        }

        return mapToResponse(saved);
    }

    @Transactional
    public ComplaintResponse updateComplaint(Long rawSchoolId, Long id, ComplaintRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        SchoolComplaint complaint = schoolComplaintRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        com.school.erp.security.AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        boolean isOwner = currentUser != null && complaint.getCreatedByUserId() != null
                && complaint.getCreatedByUserId().equals(currentUser.userId());
        boolean isAdmin = currentUser != null && currentUser.role() != null
                && (currentUser.role() == com.school.erp.entity.auth.UserRole.SUPER_ADMIN
                        || currentUser.role() == com.school.erp.entity.auth.UserRole.ADMIN);

        String oldStatus = complaint.getStatus();

        // If NOT owner and NOT admin, only status and actionTaken can be updated!
        if (isOwner || isAdmin) {
            if (request.getComplainantName() != null && !request.getComplainantName().isBlank()) {
                complaint.setComplainantName(request.getComplainantName());
            }
            complaint.setMobileNumber(request.getMobileNumber());
            if (request.getComplaintType() != null && !request.getComplaintType().isBlank()) {
                complaint.setComplaintType(request.getComplaintType());
            }
            complaint.setDescription(request.getDescription());
            complaint.setAssignedBy(request.getAssignedBy());
            if (request.getComplaintDate() != null) {
                complaint.setComplaintDate(request.getComplaintDate());
            }
            if (request.getPriority() != null && !request.getPriority().isBlank()) {
                complaint.setPriority(request.getPriority());
            }
        }

        // Status & actionTaken can be updated
        complaint.setActionTaken(request.getActionTaken());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            complaint.setStatus(request.getStatus());
        }

        SchoolComplaint saved = schoolComplaintRepository.save(complaint);
        notifyIfComplaintResolved(schoolId, oldStatus, saved);
        return mapToResponse(saved);
    }

    @Transactional
    public ComplaintResponse updateComplaintStatus(Long rawSchoolId, Long id, String status, String actionTaken) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        SchoolComplaint complaint = schoolComplaintRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        String oldStatus = complaint.getStatus();
        if (status != null && !status.isBlank()) {
            complaint.setStatus(status);
        }
        if (actionTaken != null) {
            complaint.setActionTaken(actionTaken);
        }

        SchoolComplaint saved = schoolComplaintRepository.save(complaint);
        notifyIfComplaintResolved(schoolId, oldStatus, saved);
        return mapToResponse(saved);
    }

    private void notifyIfComplaintResolved(Long schoolId, String oldStatus, SchoolComplaint saved) {
        if (notificationService == null || saved.getStatus() == null) {
            return;
        }
        boolean nowResolved = "RESOLVED".equalsIgnoreCase(saved.getStatus())
                || "CLOSED".equalsIgnoreCase(saved.getStatus());
        if (nowResolved && !saved.getStatus().equalsIgnoreCase(oldStatus)) {
            notificationService.notifySchoolAdmins(
                    schoolId,
                    NotificationType.COMPLAINT_RESOLVED,
                    NotificationPriority.MEDIUM,
                    "Complaint " + saved.getStatus() + ": " + saved.getComplainantName(),
                    "Complaint by " + saved.getComplainantName() + " (" + saved.getComplaintType()
                            + ") has been marked as " + saved.getStatus() + ".",
                    "/front-office/complaints",
                    "View Complaint",
                    "SCHOOL_COMPLAINT",
                    String.valueOf(saved.getId()),
                    Map.of(
                            "complaintId", saved.getId(),
                            "status", saved.getStatus()));
        }
    }

    @Transactional
    public void deleteComplaint(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        SchoolComplaint complaint = schoolComplaintRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        schoolComplaintRepository.delete(complaint);
    }

    @Transactional(readOnly = true)
    public ComplaintStatsResponse getStats(Long rawSchoolId) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        long total = schoolComplaintRepository.countBySchoolId(schoolId);
        long open = schoolComplaintRepository.countOpenComplaints(schoolId);
        long closed = schoolComplaintRepository.countClosedComplaints(schoolId);

        YearMonth ym = YearMonth.now();
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();
        long resolvedThisMonth = schoolComplaintRepository.countResolvedBetweenDates(schoolId, startOfMonth,
                endOfMonth);

        return ComplaintStatsResponse.builder()
                .totalComplaints(total)
                .openComplaints(open)
                .closedComplaints(closed)
                .thisMonthResolved(resolvedThisMonth)
                .build();
    }

    private ComplaintResponse mapToResponse(SchoolComplaint c) {
        com.school.erp.security.AuthenticatedUser currentUser = authContextService.getCurrentUserOrNull();
        boolean isOwn = currentUser != null && c.getCreatedByUserId() != null
                && c.getCreatedByUserId().equals(currentUser.userId());

        return ComplaintResponse.builder()
                .id(c.getId())
                .schoolId(c.getSchool().getId())
                .complaintCode(
                        c.getComplaintCode() != null ? c.getComplaintCode() : String.format("CMP-%05d", c.getId()))
                .complainantName(c.getComplainantName())
                .mobileNumber(c.getMobileNumber())
                .complaintType(c.getComplaintType())
                .description(c.getDescription())
                .assignedBy(c.getAssignedBy())
                .actionTaken(c.getActionTaken())
                .status(c.getStatus())
                .createdByUserId(c.getCreatedByUserId())
                .createdByRole(c.getCreatedByRole())
                .sourceScope(c.getSourceScope() != null ? c.getSourceScope() : "SCHOOL")
                .priority(c.getPriority() != null ? c.getPriority() : "NORMAL")
                .isOwn(isOwn)
                .complaintDate(c.getComplaintDate())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
