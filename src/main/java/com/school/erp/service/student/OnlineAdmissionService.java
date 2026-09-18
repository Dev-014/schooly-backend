package com.school.erp.service;

import com.school.erp.dto.student.OnlineAdmissionRequest;
import com.school.erp.dto.student.OnlineAdmissionResponse;
import com.school.erp.entity.OnlineAdmission;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolClass;
import com.school.erp.entity.StudentCategory;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.OnlineAdmissionRepository;
import com.school.erp.repository.SchoolClassRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentCategoryRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OnlineAdmissionService {

    private final OnlineAdmissionRepository admissionRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository classRepository;
    private final StudentCategoryRepository categoryRepository;
    private final AuthContextService authContextService;

    public OnlineAdmissionService(OnlineAdmissionRepository admissionRepository, SchoolRepository schoolRepository, SchoolClassRepository classRepository, StudentCategoryRepository categoryRepository, AuthContextService authContextService) {
        this.admissionRepository = admissionRepository;
        this.schoolRepository = schoolRepository;
        this.classRepository = classRepository;
        this.categoryRepository = categoryRepository;
        this.authContextService = authContextService;
    }

    public List<OnlineAdmissionResponse> getAdmissions(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return admissionRepository.findBySchoolId(effectiveSchoolId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OnlineAdmissionResponse createAdmission(OnlineAdmissionRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        SchoolClass schoolClass = request.classId() != null
                ? classRepository.findByIdAndSchoolId(request.classId(), effectiveSchoolId).orElse(null)
                : null;
                
        StudentCategory category = request.categoryId() != null
                ? categoryRepository.findByIdAndSchoolId(request.categoryId(), effectiveSchoolId).orElse(null)
                : null;

        OnlineAdmission admission = new OnlineAdmission();
        admission.setSchool(school);
        admission.setApplicationId("APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        admission.setStudentName(request.studentName());
        admission.setSchoolClass(schoolClass);
        admission.setFatherName(request.fatherName());
        admission.setDateOfBirth(request.dateOfBirth());
        admission.setGender(request.gender());
        admission.setCategory(category);
        admission.setMobileNumber(request.mobileNumber());
        admission.setEmail(request.email());
        admission.setAddress(request.address());
        admission.setPreviousSchool(request.previousSchool());
        admission.setTransactionStatus(request.transactionStatus() != null ? request.transactionStatus() : "UNPAID");

        return toResponse(admissionRepository.save(admission));
    }

    @Transactional
    public OnlineAdmissionResponse updateStatus(Long id, Long schoolId, String status) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        OnlineAdmission admission = admissionRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission not found"));
        admission.setStatus(status);
        return toResponse(admissionRepository.save(admission));
    }

    private OnlineAdmissionResponse toResponse(OnlineAdmission admission) {
        return new OnlineAdmissionResponse(
                admission.getId(),
                admission.getSchool().getId(),
                admission.getApplicationId(),
                admission.getStudentName(),
                admission.getSchoolClass() != null ? admission.getSchoolClass().getId() : null,
                admission.getSchoolClass() != null ? admission.getSchoolClass().getName() : null,
                admission.getFatherName(),
                admission.getDateOfBirth(),
                admission.getGender(),
                admission.getCategory() != null ? admission.getCategory().getId() : null,
                admission.getCategory() != null ? admission.getCategory().getName() : null,
                admission.getMobileNumber(),
                admission.getEmail(),
                admission.getAddress(),
                admission.getPreviousSchool(),
                admission.getTransactionStatus(),
                admission.getStatus(),
                admission.getAppliedDate()
        );
    }
}
