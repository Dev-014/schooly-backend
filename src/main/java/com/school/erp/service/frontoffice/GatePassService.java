package com.school.erp.service.frontoffice;

import com.school.erp.dto.frontoffice.GatePassRequest;
import com.school.erp.dto.frontoffice.GatePassResponse;
import com.school.erp.dto.frontoffice.GatePassStatsResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import com.school.erp.entity.Student;
import com.school.erp.entity.frontoffice.GatePass;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.repository.frontoffice.GatePassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class GatePassService {

    private final GatePassRepository gatePassRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public Page<GatePassResponse> filterGatePasses(
            Long schoolId,
            String search,
            LocalDate date,
            String role,
            String status,
            Pageable pageable) {

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanRole = (role != null && !role.isBlank() && !role.equalsIgnoreCase("all")) ? role.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<GatePass> page = gatePassRepository.filterGatePasses(schoolId, cleanSearch, date, cleanRole, cleanStatus, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public GatePassResponse getGatePassById(Long schoolId, Long id) {
        GatePass gatePass = gatePassRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Gate pass not found with id: " + id));
        return mapToResponse(gatePass);
    }

    @Transactional
    public GatePassResponse createGatePass(Long schoolId, GatePassRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        GatePass gatePass = new GatePass();
        gatePass.setSchool(school);

        if (request.getPassNumber() != null && !request.getPassNumber().isBlank()) {
            gatePass.setPassNumber(request.getPassNumber().trim());
        } else {
            long count = gatePassRepository.countIssuedOnDate(schoolId, LocalDate.now()) + 1;
            gatePass.setPassNumber(String.format("GP-%d-%04d", LocalDate.now().getYear(), count));
        }

        gatePass.setPersonName(request.getPersonName());
        gatePass.setRole(request.getRole() != null ? request.getRole().toUpperCase() : "STUDENT");
        gatePass.setClassOrDepartment(request.getClassOrDepartment());
        gatePass.setReasonForExit(request.getReasonForExit());
        gatePass.setPassDate(request.getPassDate() != null ? request.getPassDate() : LocalDate.now());
        gatePass.setExitTime(request.getExitTime() != null ? request.getExitTime() : LocalTime.now());
        gatePass.setExpectedReturnTime(request.getExpectedReturnTime());
        gatePass.setApprovedBy(request.getApprovedBy());
        gatePass.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "APPROVED");

        if (request.getStudentId() != null) {
            Student student = studentRepository.findById(request.getStudentId()).orElse(null);
            gatePass.setStudent(student);
        }
        if (request.getStaffId() != null) {
            Staff staff = staffRepository.findById(request.getStaffId()).orElse(null);
            gatePass.setStaff(staff);
        }
        if (request.getApprovedByStaffId() != null) {
            Staff approver = staffRepository.findById(request.getApprovedByStaffId()).orElse(null);
            gatePass.setApprovedByStaff(approver);
        }

        GatePass saved = gatePassRepository.save(gatePass);
        return mapToResponse(saved);
    }

    @Transactional
    public GatePassResponse updateStatus(Long schoolId, Long id, String status) {
        GatePass gatePass = gatePassRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Gate pass not found with id: " + id));

        gatePass.setStatus(status.toUpperCase());
        GatePass updated = gatePassRepository.save(gatePass);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public GatePassStatsResponse getGatePassStats(Long schoolId) {
        LocalDate today = LocalDate.now();
        long issuedToday = gatePassRepository.countIssuedOnDate(schoolId, today);
        long approvedToday = gatePassRepository.countApprovedOnDate(schoolId, today);

        return GatePassStatsResponse.builder()
                .issuedToday(issuedToday)
                .approvedToday(approvedToday)
                .build();
    }

    @Transactional
    public void deleteGatePass(Long schoolId, Long id) {
        GatePass gatePass = gatePassRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Gate pass not found with id: " + id));
        gatePassRepository.delete(gatePass);
    }

    private GatePassResponse mapToResponse(GatePass g) {
        return GatePassResponse.builder()
                .id(g.getId())
                .schoolId(g.getSchool().getId())
                .passNumber(g.getPassNumber())
                .personName(g.getPersonName())
                .role(g.getRole())
                .studentId(g.getStudent() != null ? g.getStudent().getId() : null)
                .staffId(g.getStaff() != null ? g.getStaff().getId() : null)
                .classOrDepartment(g.getClassOrDepartment())
                .reasonForExit(g.getReasonForExit())
                .passDate(g.getPassDate())
                .exitTime(g.getExitTime())
                .expectedReturnTime(g.getExpectedReturnTime())
                .approvedBy(g.getApprovedBy())
                .approvedByStaffId(g.getApprovedByStaff() != null ? g.getApprovedByStaff().getId() : null)
                .status(g.getStatus())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}
