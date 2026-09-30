package com.school.erp.service.frontoffice;

import com.school.erp.dto.frontoffice.ComplaintRequest;
import com.school.erp.dto.frontoffice.ComplaintResponse;
import com.school.erp.dto.frontoffice.ComplaintStatsResponse;
import com.school.erp.entity.frontoffice.SchoolComplaint;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.frontoffice.SchoolComplaintRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class SchoolComplaintService {

    private final AuthContextService authContextService;
    private final SchoolComplaintRepository schoolComplaintRepository;
    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> filterComplaints(
            Long rawSchoolId,
            String search,
            String complaintType,
            String status,
            Pageable pageable) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanType = (complaintType != null && !complaintType.isBlank() && !complaintType.equalsIgnoreCase("all")) ? complaintType.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<SchoolComplaint> page = schoolComplaintRepository.filterComplaints(
                schoolId, cleanSearch, cleanType, cleanStatus, pageable);
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

        SchoolComplaint complaint = new SchoolComplaint();
        complaint.setSchool(school);
        complaint.setComplainantName(request.getComplainantName());
        complaint.setMobileNumber(request.getMobileNumber());
        complaint.setComplaintType(request.getComplaintType());
        complaint.setDescription(request.getDescription());
        complaint.setAssignedBy(request.getAssignedBy());
        complaint.setActionTaken(request.getActionTaken());
        complaint.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "Open");
        complaint.setComplaintDate(request.getComplaintDate() != null ? request.getComplaintDate() : LocalDate.now());

        SchoolComplaint saved = schoolComplaintRepository.save(complaint);
        return mapToResponse(saved);
    }

    @Transactional
    public ComplaintResponse updateComplaint(Long rawSchoolId, Long id, ComplaintRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        SchoolComplaint complaint = schoolComplaintRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        if (request.getComplainantName() != null && !request.getComplainantName().isBlank()) {
            complaint.setComplainantName(request.getComplainantName());
        }
        complaint.setMobileNumber(request.getMobileNumber());
        if (request.getComplaintType() != null && !request.getComplaintType().isBlank()) {
            complaint.setComplaintType(request.getComplaintType());
        }
        complaint.setDescription(request.getDescription());
        complaint.setAssignedBy(request.getAssignedBy());
        complaint.setActionTaken(request.getActionTaken());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            complaint.setStatus(request.getStatus());
        }
        if (request.getComplaintDate() != null) {
            complaint.setComplaintDate(request.getComplaintDate());
        }

        SchoolComplaint saved = schoolComplaintRepository.save(complaint);
        return mapToResponse(saved);
    }

    @Transactional
    public ComplaintResponse updateComplaintStatus(Long rawSchoolId, Long id, String status, String actionTaken) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        SchoolComplaint complaint = schoolComplaintRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        if (status != null && !status.isBlank()) {
            complaint.setStatus(status);
        }
        if (actionTaken != null) {
            complaint.setActionTaken(actionTaken);
        }

        SchoolComplaint saved = schoolComplaintRepository.save(complaint);
        return mapToResponse(saved);
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
        long resolvedThisMonth = schoolComplaintRepository.countResolvedBetweenDates(schoolId, startOfMonth, endOfMonth);

        return ComplaintStatsResponse.builder()
                .totalComplaints(total)
                .openComplaints(open)
                .closedComplaints(closed)
                .thisMonthResolved(resolvedThisMonth)
                .build();
    }

    private ComplaintResponse mapToResponse(SchoolComplaint c) {
        return ComplaintResponse.builder()
                .id(c.getId())
                .schoolId(c.getSchool().getId())
                .complainantName(c.getComplainantName())
                .mobileNumber(c.getMobileNumber())
                .complaintType(c.getComplaintType())
                .description(c.getDescription())
                .assignedBy(c.getAssignedBy())
                .actionTaken(c.getActionTaken())
                .status(c.getStatus())
                .complaintDate(c.getComplaintDate())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
