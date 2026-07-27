package com.school.erp.service;

import com.school.erp.dto.student.StudentReferralRequest;
import com.school.erp.dto.student.StudentReferralResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.StudentReferral;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentReferralRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentReferralService {

    private final StudentReferralRepository referralRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public StudentReferralService(StudentReferralRepository referralRepository, SchoolRepository schoolRepository, AuthContextService authContextService) {
        this.referralRepository = referralRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<StudentReferralResponse> getAllReferrals(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return referralRepository.findBySchoolId(effectiveSchoolId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StudentReferralResponse createReferral(StudentReferralRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        StudentReferral referral = new StudentReferral();
        referral.setSchool(school);
        referral.setReferralBy(request.referralBy());
        referral.setStudentName(request.studentName());
        referral.setEmail(request.email());
        referral.setMobile(request.mobile());
        referral.setNote(request.note());
        if (request.status() != null && !request.status().isBlank()) {
            referral.setStatus(request.status());
        }

        return toResponse(referralRepository.save(referral));
    }

    @Transactional
    public StudentReferralResponse updateReferral(Long id, StudentReferralRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        StudentReferral referral = referralRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral not found"));

        referral.setReferralBy(request.referralBy());
        referral.setStudentName(request.studentName());
        referral.setEmail(request.email());
        referral.setMobile(request.mobile());
        referral.setNote(request.note());
        if (request.status() != null && !request.status().isBlank()) {
            referral.setStatus(request.status());
        }

        return toResponse(referralRepository.save(referral));
    }

    @Transactional
    public void deleteReferral(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        StudentReferral referral = referralRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral not found"));
        referralRepository.delete(referral);
    }

    private StudentReferralResponse toResponse(StudentReferral referral) {
        return new StudentReferralResponse(
                referral.getId(),
                referral.getSchool().getId(),
                referral.getReferralBy(),
                referral.getStudentName(),
                referral.getEmail(),
                referral.getMobile(),
                referral.getNote(),
                referral.getStatus(),
                referral.getCreatedAt()
        );
    }
}
