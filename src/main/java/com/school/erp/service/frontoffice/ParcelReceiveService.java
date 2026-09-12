package com.school.erp.service.frontoffice;

import com.school.erp.dto.frontoffice.ParcelReceiveRequest;
import com.school.erp.dto.frontoffice.ParcelReceiveResponse;
import com.school.erp.dto.frontoffice.ParcelReceiveStatsResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import com.school.erp.entity.frontoffice.ParcelReceive;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.security.AuthContextService;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.frontoffice.ParcelReceiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParcelReceiveService {

    private final AuthContextService authContextService;

    private final ParcelReceiveRepository parcelReceiveRepository;
    private final SchoolRepository schoolRepository;
    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public Page<ParcelReceiveResponse> filterParcelReceives(
            Long rawSchoolId,
            String search,
            LocalDate date,
            String status,
            Pageable pageable) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<ParcelReceive> page = parcelReceiveRepository.filterParcelReceives(schoolId, cleanSearch, date, cleanStatus, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ParcelReceiveResponse getParcelReceiveById(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        ParcelReceive parcel = parcelReceiveRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel receive log not found with id: " + id));
        return mapToResponse(parcel);
    }

    @Transactional
    public ParcelReceiveResponse receiveParcel(Long rawSchoolId, ParcelReceiveRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        ParcelReceive parcel = new ParcelReceive();
        parcel.setSchool(school);
        parcel.setSenderName(request.getSenderName());
        parcel.setContactNumber(request.getContactNumber());
        parcel.setRecipientName(request.getRecipientName());
        parcel.setRecipientType(request.getRecipientType());
        parcel.setItemDetails(request.getItemDetails());
        parcel.setDateReceived(request.getDateReceived() != null ? request.getDateReceived() : LocalDate.now());
        parcel.setReceivedBy(request.getReceivedBy());
        parcel.setStatus("RECEIVED");

        if (request.getReceivedByStaffId() != null) {
            Staff staff = staffRepository.findById(request.getReceivedByStaffId()).orElse(null);
            parcel.setReceivedByStaff(staff);
        }

        ParcelReceive saved = parcelReceiveRepository.save(parcel);
        return mapToResponse(saved);
    }

    @Transactional
    public ParcelReceiveResponse markCollected(Long rawSchoolId, Long id, String collectedBy) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        ParcelReceive parcel = parcelReceiveRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel receive log not found with id: " + id));

        parcel.setStatus("COLLECTED");
        parcel.setCollectedAt(LocalDateTime.now());
        parcel.setCollectedBy(collectedBy != null && !collectedBy.isBlank() ? collectedBy : parcel.getRecipientName());

        ParcelReceive updated = parcelReceiveRepository.save(parcel);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public ParcelReceiveStatsResponse getParcelReceiveStats(Long rawSchoolId) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        long todayCount = parcelReceiveRepository.countReceivedOnDate(schoolId, today);
        long yesterdayCount = parcelReceiveRepository.countReceivedOnDate(schoolId, yesterday);
        long changeFromYesterday = todayCount - yesterdayCount;

        long pendingPickup = parcelReceiveRepository.countPendingPickup(schoolId);

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        long completedThisMonth = parcelReceiveRepository.countCompletedInPeriod(schoolId, startOfMonth, endOfMonth);

        return ParcelReceiveStatsResponse.builder()
                .todayReceived(todayCount)
                .changeFromYesterday(changeFromYesterday)
                .pendingPickup(pendingPickup)
                .completedThisMonth(completedThisMonth)
                .build();
    }

    @Transactional
    public void deleteParcelReceive(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        
        ParcelReceive parcel = parcelReceiveRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel receive log not found with id: " + id));
        parcelReceiveRepository.delete(parcel);
    }

    private ParcelReceiveResponse mapToResponse(ParcelReceive p) {
        return ParcelReceiveResponse.builder()
                .id(p.getId())
                .schoolId(p.getSchool().getId())
                .senderName(p.getSenderName())
                .contactNumber(p.getContactNumber())
                .recipientName(p.getRecipientName())
                .recipientType(p.getRecipientType())
                .itemDetails(p.getItemDetails())
                .dateReceived(p.getDateReceived())
                .receivedBy(p.getReceivedBy())
                .receivedByStaffId(p.getReceivedByStaff() != null ? p.getReceivedByStaff().getId() : null)
                .receivedByStaffName(p.getReceivedByStaff() != null ? formatStaffName(p.getReceivedByStaff()) : null)
                .status(p.getStatus())
                .collectedAt(p.getCollectedAt())
                .collectedBy(p.getCollectedBy())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private String formatStaffName(Staff staff) {
        String first = staff.getFirstName() != null ? staff.getFirstName() : "";
        String last = staff.getLastName() != null ? staff.getLastName() : "";
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }
}
