package com.school.erp.service;

import com.school.erp.dto.student.IdCardGenerationRequest;
import com.school.erp.dto.student.IdCardGenerationResponse;
import com.school.erp.entity.IdCardGeneration;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.IdCardGenerationRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class IdCardGenerationService {

    private final IdCardGenerationRepository idCardRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public IdCardGenerationService(IdCardGenerationRepository idCardRepository, StudentRepository studentRepository, SchoolRepository schoolRepository, AuthContextService authContextService) {
        this.idCardRepository = idCardRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<IdCardGenerationResponse> getAllGenerations(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return idCardRepository.findBySchoolId(effectiveSchoolId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public IdCardGenerationResponse generateOrUpdateIdCard(IdCardGenerationRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        
        Optional<IdCardGeneration> existing = idCardRepository.findByStudentIdAndSchoolId(request.studentId(), effectiveSchoolId);
        if (existing.isPresent()) {
            IdCardGeneration card = existing.get();
            if (request.status() != null && !request.status().isBlank()) {
                card.setStatus(request.status());
            }
            return toResponse(idCardRepository.save(card));
        }

        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        if (!student.getSchool().getId().equals(effectiveSchoolId)) {
            throw new IllegalArgumentException("Student does not belong to this school");
        }

        IdCardGeneration card = new IdCardGeneration();
        card.setSchool(school);
        card.setStudent(student);
        if (request.status() != null && !request.status().isBlank()) {
            card.setStatus(request.status());
        }

        return toResponse(idCardRepository.save(card));
    }

    private IdCardGenerationResponse toResponse(IdCardGeneration card) {
        String classKey = card.getStudent().getSchoolClass() != null ? card.getStudent().getSchoolClass().getName() : "";
        return new IdCardGenerationResponse(
                card.getId(),
                card.getSchool().getId(),
                card.getStudent().getId(),
                card.getStudent().getFirstName() + " " + card.getStudent().getLastName(),
                card.getStudent().getAdmissionNo(),
                classKey,
                card.getStatus(),
                card.getGeneratedAt()
        );
    }
}
