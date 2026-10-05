package com.school.erp.service.hr;

import com.school.erp.dto.hr.RecruitmentCandidateDTO;
import com.school.erp.dto.hr.RecruitmentCandidateRequest;
import com.school.erp.entity.hr.RecruitmentCandidate;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.hr.RecruitmentCandidateRepository;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentCandidateService {

    private final RecruitmentCandidateRepository candidateRepository;
    private final SchoolRepository schoolRepository;
    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public List<RecruitmentCandidateDTO> getCandidates(Long schoolId, String role, String status, String search) {
        List<RecruitmentCandidate> candidates = candidateRepository.findBySchoolId(schoolId);

        if (role != null && !role.trim().isEmpty()) {
            candidates = candidates.stream()
                    .filter(c -> c.getApplyingFor() != null && c.getApplyingFor().equalsIgnoreCase(role.trim()))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            candidates = candidates.stream()
                    .filter(c -> c.getStatus() != null && c.getStatus().equalsIgnoreCase(status.trim()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            candidates = candidates.stream()
                    .filter(c -> (c.getName() != null && c.getName().toLowerCase().contains(q)) ||
                            (c.getEmail() != null && c.getEmail().toLowerCase().contains(q)) ||
                            (c.getPhone() != null && c.getPhone().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        return candidates.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecruitmentCandidateDTO getCandidateById(Long schoolId, Long id) {
        RecruitmentCandidate candidate = candidateRepository.findById(id)
                .filter(c -> c.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        return mapToDTO(candidate);
    }

    @Transactional
    public RecruitmentCandidateDTO createCandidate(Long schoolId, RecruitmentCandidateRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        RecruitmentCandidate candidate = new RecruitmentCandidate();
        candidate.setSchool(school);
        copyRequestToEntity(request, candidate);
        candidate.setSubmissionDate(LocalDateTime.now());
        if (candidate.getStatus() == null || candidate.getStatus().trim().isEmpty()) {
            candidate.setStatus("NEW");
        }

        return mapToDTO(candidateRepository.save(candidate));
    }

    @Transactional
    public RecruitmentCandidateDTO updateCandidate(Long schoolId, Long id, RecruitmentCandidateRequest request) {
        RecruitmentCandidate candidate = candidateRepository.findById(id)
                .filter(c -> c.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        copyRequestToEntity(request, candidate);
        return mapToDTO(candidateRepository.save(candidate));
    }

    @Transactional
    public RecruitmentCandidateDTO updateCandidateStatus(Long schoolId, Long id, String status) {
        RecruitmentCandidate candidate = candidateRepository.findById(id)
                .filter(c -> c.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidate.setStatus(status != null ? status.toUpperCase() : "NEW");
        return mapToDTO(candidateRepository.save(candidate));
    }

    @Transactional
    public Staff convertCandidateToStaff(Long schoolId, Long id) {
        RecruitmentCandidate candidate = candidateRepository.findById(id)
                .filter(c -> c.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        String name = candidate.getName() != null ? candidate.getName().trim() : "Staff";
        String firstName = name;
        String lastName = "";
        int spaceIndex = name.indexOf(' ');
        if (spaceIndex > 0) {
            firstName = name.substring(0, spaceIndex).trim();
            lastName = name.substring(spaceIndex + 1).trim();
        }

        Staff staff = new Staff();
        staff.setSchool(candidate.getSchool());
        staff.setFirstName(firstName);
        staff.setLastName(lastName);
        staff.setEmail(candidate.getEmail());
        staff.setPhone(candidate.getPhone());
        staff.setDateOfBirth(candidate.getDateOfBirth());
        staff.setDesignation(candidate.getApplyingFor() != null ? candidate.getApplyingFor() : "Staff Member");
        staff.setMaritalStatus(candidate.getMaritalStatus());
        staff.setWorkExperience(candidate.getWorkExperience());
        staff.setSalary(candidate.getExpectedSalary());
        staff.setJoiningDate(LocalDate.now());
        staff.setStatus("ACTIVE");

        Staff savedStaff = staffRepository.save(staff);

        candidate.setStatus("HIRED");
        candidateRepository.save(candidate);

        return savedStaff;
    }

    @Transactional
    public void deleteCandidate(Long schoolId, Long id) {
        RecruitmentCandidate candidate = candidateRepository.findById(id)
                .filter(c -> c.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        candidateRepository.delete(candidate);
    }

    private void copyRequestToEntity(RecruitmentCandidateRequest request, RecruitmentCandidate candidate) {
        candidate.setName(request.getName());
        candidate.setPhone(request.getPhone());
        candidate.setEmail(request.getEmail());
        candidate.setDateOfBirth(request.getDateOfBirth());
        candidate.setApplyingFor(request.getApplyingFor());
        candidate.setExpectedSalary(request.getExpectedSalary());
        candidate.setMaritalStatus(request.getMaritalStatus());
        candidate.setWorkExperience(request.getWorkExperience());
        candidate.setInterviewDate(request.getInterviewDate());
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            candidate.setStatus(request.getStatus().toUpperCase());
        }
        candidate.setDescription(request.getDescription());
        candidate.setDocumentUrl(request.getDocumentUrl());
    }

    private RecruitmentCandidateDTO mapToDTO(RecruitmentCandidate c) {
        return RecruitmentCandidateDTO.builder()
                .id(c.getId())
                .schoolId(c.getSchool().getId())
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .dateOfBirth(c.getDateOfBirth())
                .applyingFor(c.getApplyingFor())
                .expectedSalary(c.getExpectedSalary())
                .maritalStatus(c.getMaritalStatus())
                .workExperience(c.getWorkExperience())
                .interviewDate(c.getInterviewDate())
                .submissionDate(c.getSubmissionDate())
                .status(c.getStatus())
                .description(c.getDescription())
                .documentUrl(c.getDocumentUrl())
                .build();
    }
}
