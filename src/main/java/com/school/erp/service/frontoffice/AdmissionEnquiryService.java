package com.school.erp.service.frontoffice;

import com.school.erp.dto.frontoffice.*;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolClass;
import com.school.erp.entity.Staff;
import com.school.erp.entity.frontoffice.AdmissionEnquiry;
import com.school.erp.entity.frontoffice.AdmissionEnquiryFollowUp;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.security.AuthContextService;
import com.school.erp.repository.SchoolClassRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.frontoffice.AdmissionEnquiryFollowUpRepository;
import com.school.erp.repository.frontoffice.AdmissionEnquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdmissionEnquiryService {

    private final AuthContextService authContextService;

    private final AdmissionEnquiryRepository enquiryRepository;
    private final AdmissionEnquiryFollowUpRepository followUpRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public Page<AdmissionEnquiryResponse> filterEnquiries(
            Long rawSchoolId,
            String search,
            LocalDate startDate,
            LocalDate endDate,
            String source,
            String status,
            Pageable pageable) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanSource = (source != null && !source.isBlank() && !source.equalsIgnoreCase("all") && !source.equalsIgnoreCase("all sources")) ? source.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all") && !status.equalsIgnoreCase("all status")) ? status.trim() : null;

        Page<AdmissionEnquiry> page = enquiryRepository.filterEnquiries(schoolId, cleanSearch, startDate, endDate, cleanSource, cleanStatus, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public AdmissionEnquiryResponse getEnquiryById(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        AdmissionEnquiry enquiry = enquiryRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission enquiry not found with id: " + id));
        return mapToResponse(enquiry);
    }

    @Transactional
    public AdmissionEnquiryResponse createEnquiry(Long rawSchoolId, AdmissionEnquiryRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        AdmissionEnquiry enquiry = new AdmissionEnquiry();
        enquiry.setSchool(school);

        if (request.getEnquiryNumber() != null && !request.getEnquiryNumber().isBlank()) {
            enquiry.setEnquiryNumber(request.getEnquiryNumber().trim());
        } else {
            long count = enquiryRepository.countBySchoolId(schoolId) + 1;
            enquiry.setEnquiryNumber(String.format("#ADM-%d-%03d", LocalDate.now().getYear(), count));
        }

        populateEnquiryFields(enquiry, request);
        AdmissionEnquiry saved = enquiryRepository.save(enquiry);
        return mapToResponse(saved);
    }

    @Transactional
    public AdmissionEnquiryResponse updateEnquiry(Long rawSchoolId, Long id, AdmissionEnquiryRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        AdmissionEnquiry enquiry = enquiryRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission enquiry not found with id: " + id));

        if (request.getEnquiryNumber() != null && !request.getEnquiryNumber().isBlank()) {
            enquiry.setEnquiryNumber(request.getEnquiryNumber().trim());
        }
        populateEnquiryFields(enquiry, request);
        AdmissionEnquiry updated = enquiryRepository.save(enquiry);
        return mapToResponse(updated);
    }

    @Transactional
    public EnquiryFollowUpResponse addFollowUp(Long rawSchoolId, Long enquiryId, EnquiryFollowUpRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        AdmissionEnquiry enquiry = enquiryRepository.findByIdAndSchoolId(enquiryId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission enquiry not found with id: " + enquiryId));

        AdmissionEnquiryFollowUp followUp = new AdmissionEnquiryFollowUp();
        followUp.setEnquiry(enquiry);
        followUp.setActionType(request.getActionType());
        followUp.setNotes(request.getNotes());
        followUp.setFollowUpDate(request.getFollowUpDate() != null ? request.getFollowUpDate() : LocalDateTime.now());

        if (request.getRecordedByStaffId() != null) {
            Staff staff = staffRepository.findById(request.getRecordedByStaffId()).orElse(null);
            followUp.setRecordedBy(staff);
        }

        if (request.getNextFollowUpDate() != null) {
            enquiry.setNextFollowUpDate(request.getNextFollowUpDate());
        }
        if (request.getUpdatedStatus() != null && !request.getUpdatedStatus().isBlank()) {
            enquiry.setStatus(request.getUpdatedStatus().toUpperCase());
        }

        AdmissionEnquiryFollowUp saved = followUpRepository.save(followUp);
        enquiryRepository.save(enquiry);

        return mapToFollowUpResponse(saved);
    }

    @Transactional(readOnly = true)
    public AdmissionEnquiryStatsResponse getEnquiryStats(Long rawSchoolId) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        LocalDate today = LocalDate.now();
        long total = enquiryRepository.countBySchoolId(schoolId);
        long active = enquiryRepository.countBySchoolIdAndStatusIgnoreCase(schoolId, "ACTIVE");
        long converted = enquiryRepository.countBySchoolIdAndStatusIgnoreCase(schoolId, "CONVERTED");
        long passive = enquiryRepository.countBySchoolIdAndStatusIgnoreCase(schoolId, "PASSIVE");
        long lost = enquiryRepository.countBySchoolIdAndStatusIgnoreCase(schoolId, "LOST");
        long dueToday = enquiryRepository.countFollowUpsDueOn(schoolId, today);
        long overdue = enquiryRepository.countFollowUpsOverdue(schoolId, today);

        return AdmissionEnquiryStatsResponse.builder()
                .totalEnquiries(total)
                .activeEnquiries(active)
                .convertedEnquiries(converted)
                .passiveEnquiries(passive)
                .lostEnquiries(lost)
                .followUpsDueToday(dueToday)
                .followUpsOverdue(overdue)
                .build();
    }

    @Transactional
    public void deleteEnquiry(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        AdmissionEnquiry enquiry = enquiryRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission enquiry not found with id: " + id));
        enquiryRepository.delete(enquiry);
    }

    private void populateEnquiryFields(AdmissionEnquiry enquiry, AdmissionEnquiryRequest request) {
        enquiry.setStudentName(request.getStudentName());
        enquiry.setParentName(request.getParentName());
        enquiry.setPhone(request.getPhone());
        enquiry.setEmail(request.getEmail());
        enquiry.setEnquiryDate(request.getEnquiryDate());
        enquiry.setNextFollowUpDate(request.getNextFollowUpDate());
        enquiry.setSource(request.getSource() != null ? request.getSource() : "Website");
        enquiry.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE");
        enquiry.setNumberOfChildren(request.getNumberOfChildren() != null ? request.getNumberOfChildren() : 1);
        enquiry.setDetailedNotes(request.getDetailedNotes());

        if (request.getClassId() != null) {
            SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId()).orElse(null);
            enquiry.setSchoolClass(schoolClass);
        } else {
            enquiry.setSchoolClass(null);
        }

        if (request.getAssignedToStaffId() != null) {
            Staff staff = staffRepository.findById(request.getAssignedToStaffId()).orElse(null);
            enquiry.setAssignedTo(staff);
        }
    }

    private AdmissionEnquiryResponse mapToResponse(AdmissionEnquiry e) {
        String followUpTag = null;
        if (e.getNextFollowUpDate() != null) {
            LocalDate today = LocalDate.now();
            if (e.getNextFollowUpDate().isEqual(today)) {
                followUpTag = "TODAY";
            } else if (e.getNextFollowUpDate().isBefore(today) && "ACTIVE".equalsIgnoreCase(e.getStatus())) {
                followUpTag = "OVERDUE";
            } else {
                followUpTag = "UPCOMING";
            }
        }

        List<EnquiryFollowUpResponse> followUpsList = null;
        if (e.getFollowUps() != null) {
            followUpsList = e.getFollowUps().stream()
                    .map(this::mapToFollowUpResponse)
                    .collect(Collectors.toList());
        }

        return AdmissionEnquiryResponse.builder()
                .id(e.getId())
                .schoolId(e.getSchool().getId())
                .enquiryNumber(e.getEnquiryNumber())
                .studentName(e.getStudentName())
                .parentName(e.getParentName())
                .phone(e.getPhone())
                .email(e.getEmail())
                .enquiryDate(e.getEnquiryDate())
                .nextFollowUpDate(e.getNextFollowUpDate())
                .followUpTag(followUpTag)
                .source(e.getSource())
                .status(e.getStatus())
                .classId(e.getSchoolClass() != null ? e.getSchoolClass().getId() : null)
                .className(e.getSchoolClass() != null ? e.getSchoolClass().getName() : null)
                .numberOfChildren(e.getNumberOfChildren())
                .assignedToStaffId(e.getAssignedTo() != null ? e.getAssignedTo().getId() : null)
                .assignedToStaffName(e.getAssignedTo() != null ? formatStaffName(e.getAssignedTo()) : null)
                .detailedNotes(e.getDetailedNotes())
                .followUps(followUpsList)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private String formatStaffName(Staff staff) {
        String first = staff.getFirstName() != null ? staff.getFirstName() : "";
        String last = staff.getLastName() != null ? staff.getLastName() : "";
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }

    private EnquiryFollowUpResponse mapToFollowUpResponse(AdmissionEnquiryFollowUp f) {
        return EnquiryFollowUpResponse.builder()
                .id(f.getId())
                .actionType(f.getActionType())
                .notes(f.getNotes())
                .followUpDate(f.getFollowUpDate())
                .recordedById(f.getRecordedBy() != null ? f.getRecordedBy().getId() : null)
                .recordedByName(f.getRecordedBy() != null ? formatStaffName(f.getRecordedBy()) : null)
                .createdAt(f.getCreatedAt())
                .build();
    }
}
