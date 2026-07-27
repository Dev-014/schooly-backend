package com.school.erp.service;

import com.school.erp.dto.student.StudentSiblingRequest;
import com.school.erp.dto.student.StudentSiblingResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.entity.StudentSibling;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.repository.StudentSiblingRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentSiblingService {

    private final StudentSiblingRepository siblingRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public StudentSiblingService(StudentSiblingRepository siblingRepository, StudentRepository studentRepository, SchoolRepository schoolRepository, AuthContextService authContextService) {
        this.siblingRepository = siblingRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<StudentSiblingResponse> getSiblings(Long primaryStudentId, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return siblingRepository.findByPrimaryStudentIdAndSchoolId(primaryStudentId, effectiveSchoolId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StudentSiblingResponse linkSibling(StudentSiblingRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        Student primary = studentRepository.findById(request.primaryStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Primary student not found"));
        Student sibling = studentRepository.findById(request.siblingStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Sibling student not found"));

        // Optionally check if already linked
        siblingRepository.findByPrimaryStudentIdAndSiblingStudentIdAndSchoolId(
                primary.getId(), sibling.getId(), effectiveSchoolId).ifPresent(s -> {
            throw new IllegalArgumentException("Students are already linked as siblings");
        });

        StudentSibling studentSibling = new StudentSibling();
        studentSibling.setSchool(school);
        studentSibling.setPrimaryStudent(primary);
        studentSibling.setSiblingStudent(sibling);
        studentSibling.setRelationship(request.relationship());

        StudentSibling saved = siblingRepository.save(studentSibling);

        // Auto-link the reverse direction as well
        if (siblingRepository.findByPrimaryStudentIdAndSiblingStudentIdAndSchoolId(
                sibling.getId(), primary.getId(), effectiveSchoolId).isEmpty()) {
            StudentSibling reverse = new StudentSibling();
            reverse.setSchool(school);
            reverse.setPrimaryStudent(sibling);
            reverse.setSiblingStudent(primary);
            // simple reverse mapping assumption, can be more complex
            reverse.setRelationship(request.relationship().equals("Brother") ? "Sibling" : 
                                    request.relationship().equals("Sister") ? "Sibling" : request.relationship());
            siblingRepository.save(reverse);
        }

        return toResponse(saved);
    }

    @Transactional
    public void unlinkSibling(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        StudentSibling sibling = siblingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sibling link not found"));
        
        if (!sibling.getSchool().getId().equals(effectiveSchoolId)) {
            throw new ResourceNotFoundException("Sibling link not found in this school");
        }

        // Also delete the reverse link if exists
        siblingRepository.findByPrimaryStudentIdAndSiblingStudentIdAndSchoolId(
                sibling.getSiblingStudent().getId(), sibling.getPrimaryStudent().getId(), effectiveSchoolId)
                .ifPresent(siblingRepository::delete);

        siblingRepository.delete(sibling);
    }

    private StudentSiblingResponse toResponse(StudentSibling link) {
        Student sib = link.getSiblingStudent();
        String sibClass = sib.getSchoolClass() != null ? sib.getSchoolClass().getName() : "N/A";
        // Section name would require fetching section, assuming simple string or id here
        String sibSection = "Section " + (sib.getSectionId() != null ? sib.getSectionId() : "N/A");
        
        // Use firstName + lastName for full name if name is null
        String fullName = sib.getName();
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = (sib.getFirstName() != null ? sib.getFirstName() : "") + 
                       (sib.getLastName() != null ? " " + sib.getLastName() : "");
            fullName = fullName.trim();
        }
        
        return new StudentSiblingResponse(
                link.getId(),
                link.getSchool().getId(),
                link.getPrimaryStudent().getId(),
                sib.getId(),
                fullName,
                sib.getAdmissionNo(),
                sibClass,
                sibSection,
                sib.getPhotoUrl(),
                link.getRelationship(),
                link.getCreatedAt() != null ? link.getCreatedAt().toString() : null
        );
    }
}
