package com.school.erp.service.frontoffice;

import com.school.erp.dto.frontoffice.VisitorLogRequest;
import com.school.erp.dto.frontoffice.VisitorLogResponse;
import com.school.erp.dto.frontoffice.VisitorStatsResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import com.school.erp.entity.frontoffice.VisitorLog;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.frontoffice.VisitorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class VisitorLogService {

    private final VisitorLogRepository visitorLogRepository;
    private final SchoolRepository schoolRepository;
    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public Page<VisitorLogResponse> filterVisitors(
            Long schoolId,
            String search,
            LocalDate date,
            String purpose,
            String status,
            Pageable pageable) {

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanPurpose = (purpose != null && !purpose.isBlank() && !purpose.equalsIgnoreCase("all")) ? purpose.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<VisitorLog> page = visitorLogRepository.filterVisitors(schoolId, cleanSearch, date, cleanPurpose, cleanStatus, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public VisitorLogResponse getVisitorById(Long schoolId, Long id) {
        VisitorLog visitor = visitorLogRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor log not found with id: " + id));
        return mapToResponse(visitor);
    }

    @Transactional
    public VisitorLogResponse createVisitor(Long schoolId, VisitorLogRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        VisitorLog visitor = new VisitorLog();
        visitor.setSchool(school);
        visitor.setVisitorName(request.getVisitorName());
        visitor.setPurpose(request.getPurpose());
        visitor.setMeetingWith(request.getMeetingWith());
        visitor.setPhone(request.getPhone());
        visitor.setEmail(request.getEmail());
        visitor.setNumberOfPeople(request.getNumberOfPeople() != null ? request.getNumberOfPeople() : 1);
        visitor.setIdCardNumber(request.getIdCardNumber());
        visitor.setVisitDate(request.getVisitDate() != null ? request.getVisitDate() : LocalDate.now());
        visitor.setTimeIn(request.getTimeIn() != null ? request.getTimeIn() : LocalTime.now());
        visitor.setEstTimeOut(request.getEstTimeOut());
        visitor.setStatus("ON_SITE");
        visitor.setNote(request.getNote());

        if (request.getMeetingWithStaffId() != null) {
            Staff staff = staffRepository.findById(request.getMeetingWithStaffId()).orElse(null);
            visitor.setMeetingWithStaff(staff);
        }

        VisitorLog saved = visitorLogRepository.save(visitor);
        return mapToResponse(saved);
    }

    @Transactional
    public VisitorLogResponse checkoutVisitor(Long schoolId, Long id, LocalTime timeOut) {
        VisitorLog visitor = visitorLogRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor log not found with id: " + id));

        visitor.setStatus("CHECKED_OUT");
        visitor.setTimeOut(timeOut != null ? timeOut : LocalTime.now());

        VisitorLog saved = visitorLogRepository.save(visitor);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public VisitorStatsResponse getVisitorStats(Long schoolId) {
        LocalDate today = LocalDate.now();
        long todayVisitors = visitorLogRepository.sumGuestsForDate(schoolId, today);
        long activeCheckIns = visitorLogRepository.countActiveOnSite(schoolId);

        return VisitorStatsResponse.builder()
                .todayVisitors(todayVisitors)
                .activeCheckIns(activeCheckIns)
                .build();
    }

    @Transactional
    public void deleteVisitor(Long schoolId, Long id) {
        VisitorLog visitor = visitorLogRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor log not found with id: " + id));
        visitorLogRepository.delete(visitor);
    }

    private VisitorLogResponse mapToResponse(VisitorLog v) {
        return VisitorLogResponse.builder()
                .id(v.getId())
                .schoolId(v.getSchool().getId())
                .visitorName(v.getVisitorName())
                .purpose(v.getPurpose())
                .meetingWith(v.getMeetingWith())
                .meetingWithStaffId(v.getMeetingWithStaff() != null ? v.getMeetingWithStaff().getId() : null)
                .meetingWithStaffName(v.getMeetingWithStaff() != null ? formatStaffName(v.getMeetingWithStaff()) : null)
                .phone(v.getPhone())
                .email(v.getEmail())
                .numberOfPeople(v.getNumberOfPeople())
                .idCardNumber(v.getIdCardNumber())
                .visitDate(v.getVisitDate())
                .timeIn(v.getTimeIn())
                .estTimeOut(v.getEstTimeOut())
                .timeOut(v.getTimeOut())
                .status(v.getStatus())
                .note(v.getNote())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }

    private String formatStaffName(Staff staff) {
        String first = staff.getFirstName() != null ? staff.getFirstName() : "";
        String last = staff.getLastName() != null ? staff.getLastName() : "";
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }
}
