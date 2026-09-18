package com.school.erp.service;

import com.school.erp.dto.student.StudentCertificateRequest;
import com.school.erp.dto.student.StudentCertificateResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.entity.StudentCertificate;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentCertificateRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentCertificateService {

    private final StudentCertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public StudentCertificateService(StudentCertificateRepository certificateRepository, StudentRepository studentRepository, SchoolRepository schoolRepository, AuthContextService authContextService) {
        this.certificateRepository = certificateRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<StudentCertificateResponse> getAllCertificates(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return certificateRepository.findBySchoolId(effectiveSchoolId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StudentCertificateResponse createCertificate(StudentCertificateRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!student.getSchool().getId().equals(effectiveSchoolId)) {
            throw new IllegalArgumentException("Student does not belong to this school");
        }

        StudentCertificate certificate = new StudentCertificate();
        certificate.setSchool(school);
        certificate.setStudent(student);
        certificate.setCertificateType(request.certificateType());
        certificate.setIssueDate(request.issueDate());
        certificate.setRemarks(request.remarks());
        if (request.status() != null && !request.status().isBlank()) {
            certificate.setStatus(request.status());
        }

        return toResponse(certificateRepository.save(certificate));
    }

    @Transactional
    public StudentCertificateResponse updateCertificate(Long id, StudentCertificateRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        StudentCertificate certificate = certificateRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));

        certificate.setCertificateType(request.certificateType());
        certificate.setIssueDate(request.issueDate());
        certificate.setRemarks(request.remarks());
        if (request.status() != null && !request.status().isBlank()) {
            certificate.setStatus(request.status());
        }

        return toResponse(certificateRepository.save(certificate));
    }

    @Transactional
    public void deleteCertificate(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        StudentCertificate certificate = certificateRepository.findByIdAndSchoolId(id, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
        certificateRepository.delete(certificate);
    }

    private StudentCertificateResponse toResponse(StudentCertificate certificate) {
        return new StudentCertificateResponse(
                certificate.getId(),
                certificate.getSchool().getId(),
                certificate.getStudent().getId(),
                certificate.getStudent().getFirstName() + " " + certificate.getStudent().getLastName(),
                certificate.getStudent().getAdmissionNo(),
                certificate.getCertificateType(),
                certificate.getIssueDate(),
                certificate.getStatus(),
                certificate.getRemarks(),
                certificate.getCreatedAt()
        );
    }
}
