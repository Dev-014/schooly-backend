package com.school.erp.service.hr;

import com.school.erp.dto.hr.StaffCertificateDTO;
import com.school.erp.dto.hr.StaffCertificateRequest;
import com.school.erp.dto.hr.StaffIdCardDTO;
import com.school.erp.dto.hr.StaffIdCardGenerationRequest;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.hr.StaffCertificate;
import com.school.erp.entity.hr.StaffIdCard;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.hr.StaffCertificateRepository;
import com.school.erp.repository.hr.StaffIdCardRepository;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffCertificateAndIdCardService {

    private final StaffCertificateRepository certificateRepository;
    private final StaffIdCardRepository idCardRepository;
    private final StaffRepository staffRepository;
    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public List<StaffCertificateDTO> getCertificates(Long schoolId, Long staffId, String certificateType, String status) {
        List<StaffCertificate> certs = (staffId != null)
                ? certificateRepository.findBySchoolIdAndStaffId(schoolId, staffId)
                : certificateRepository.findBySchoolId(schoolId);

        if (certificateType != null && !certificateType.trim().isEmpty()) {
            certs = certs.stream()
                    .filter(c -> c.getCertificateType() != null && c.getCertificateType().equalsIgnoreCase(certificateType.trim()))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            certs = certs.stream()
                    .filter(c -> c.getStatus() != null && c.getStatus().equalsIgnoreCase(status.trim()))
                    .collect(Collectors.toList());
        }

        return certs.stream().map(this::mapCertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public StaffCertificateDTO generateCertificate(Long schoolId, StaffCertificateRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        Staff staff = staffRepository.findByIdAndSchoolId(request.getStaffId(), schoolId)
                .orElseThrow(() -> new RuntimeException("Staff member not found"));

        StaffCertificate cert = new StaffCertificate();
        cert.setSchool(school);
        cert.setStaff(staff);
        cert.setCertificateType(request.getCertificateType().toUpperCase());
        cert.setIssueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now());
        cert.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "ISSUED");
        cert.setRemarks(request.getRemarks());

        return mapCertToDTO(certificateRepository.save(cert));
    }

    @Transactional(readOnly = true)
    public List<StaffIdCardDTO> getIdCards(Long schoolId) {
        return idCardRepository.findBySchoolId(schoolId).stream()
                .map(this::mapIdCardToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<StaffIdCardDTO> generateIdCards(Long schoolId, StaffIdCardGenerationRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        List<Staff> staffList;
        if (request != null && request.getStaffIds() != null && !request.getStaffIds().isEmpty()) {
            staffList = staffRepository.findAllById(request.getStaffIds()).stream()
                    .filter(s -> s.getSchool().getId().equals(schoolId))
                    .collect(Collectors.toList());
        } else {
            staffList = staffRepository.findBySchoolId(schoolId);
        }

        String template = request != null && request.getTemplate() != null ? request.getTemplate() : "DEFAULT";
        List<StaffIdCardDTO> result = new ArrayList<>();

        for (Staff s : staffList) {
            StaffIdCard card = new StaffIdCard();
            card.setSchool(school);
            card.setStaff(s);
            card.setStatus("GENERATED");
            card.setTemplate(template);
            card.setGeneratedAt(LocalDateTime.now());
            result.add(mapIdCardToDTO(idCardRepository.save(card)));
        }

        return result;
    }

    private StaffCertificateDTO mapCertToDTO(StaffCertificate c) {
        Staff s = c.getStaff();
        String staffName = s != null
                ? ((s.getFirstName() != null ? s.getFirstName() : "") + " " +
                (s.getLastName() != null ? s.getLastName() : "")).trim()
                : "Staff #" + (s != null ? s.getId() : "");
        String staffCode = s != null && s.getBiometricId() != null
                ? s.getBiometricId()
                : "STF-" + String.format("%04d", s != null ? s.getId() : 0);

        return StaffCertificateDTO.builder()
                .id(c.getId())
                .schoolId(c.getSchool().getId())
                .staffId(s != null ? s.getId() : null)
                .staffName(staffName)
                .staffCode(staffCode)
                .department(s != null ? s.getDepartment() : "General")
                .designation(s != null ? s.getDesignation() : "Staff Member")
                .certificateType(c.getCertificateType())
                .issueDate(c.getIssueDate())
                .status(c.getStatus())
                .remarks(c.getRemarks())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private StaffIdCardDTO mapIdCardToDTO(StaffIdCard card) {
        Staff s = card.getStaff();
        String staffName = s != null
                ? ((s.getFirstName() != null ? s.getFirstName() : "") + " " +
                (s.getLastName() != null ? s.getLastName() : "")).trim()
                : "Staff #" + (s != null ? s.getId() : "");
        String staffCode = s != null && s.getBiometricId() != null
                ? s.getBiometricId()
                : "STF-" + String.format("%04d", s != null ? s.getId() : 0);

        return StaffIdCardDTO.builder()
                .id(card.getId())
                .schoolId(card.getSchool().getId())
                .staffId(s != null ? s.getId() : null)
                .staffName(staffName)
                .staffCode(staffCode)
                .department(s != null ? s.getDepartment() : "General")
                .designation(s != null ? s.getDesignation() : "Staff Member")
                .photoUrl(s != null ? s.getPhotoUrl() : null)
                .status(card.getStatus())
                .template(card.getTemplate())
                .generatedAt(card.getGeneratedAt())
                .build();
    }
}
