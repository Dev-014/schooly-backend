package com.school.erp.service;

import com.school.erp.dto.student.FamilyRequest;
import com.school.erp.dto.student.FamilyResponse;
import com.school.erp.entity.Family;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.FamilyRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final AuthContextService authContextService;

    public FamilyService(FamilyRepository familyRepository, SchoolRepository schoolRepository, 
                        StudentRepository studentRepository, AuthContextService authContextService) {
        this.familyRepository = familyRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.authContextService = authContextService;
    }

    public List<FamilyResponse> getAllFamilies(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return familyRepository.findBySchoolId(effectiveSchoolId).stream()
                .map(this::toResponse)
                .toList();
    }

    public FamilyResponse getFamilyById(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Family not found"));
        
        if (!family.getSchool().getId().equals(effectiveSchoolId)) {
            throw new ResourceNotFoundException("Family not found in this school");
        }
        
        return toResponse(family);
    }

    @Transactional
    public FamilyResponse createFamily(FamilyRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.getSchoolId());
        School school = schoolRepository.findById(effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        Family family = new Family();
        family.setSchool(school);
        family.setFamilyCode(generateFamilyCode());
        family.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");

        return toResponse(familyRepository.save(family));
    }

    @Transactional
    public FamilyResponse updateFamily(Long id, FamilyRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.getSchoolId());
        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Family not found"));
        
        if (!family.getSchool().getId().equals(effectiveSchoolId)) {
            throw new ResourceNotFoundException("Family not found in this school");
        }

        if (request.getStatus() != null) {
            family.setStatus(request.getStatus());
        }

        return toResponse(familyRepository.save(family));
    }

    @Transactional
    public void deleteFamily(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Family not found"));
        
        if (!family.getSchool().getId().equals(effectiveSchoolId)) {
            throw new ResourceNotFoundException("Family not found in this school");
        }

        // Unlink all students from this family
        List<Student> students = studentRepository.findByFamilyId(id);
        students.forEach(student -> student.setFamily(null));
        studentRepository.saveAll(students);

        familyRepository.delete(family);
    }

    @Transactional
    public FamilyResponse mergeFamilies(Long sourceFamilyId, Long targetFamilyId, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        
        Family sourceFamily = familyRepository.findById(sourceFamilyId)
                .orElseThrow(() -> new ResourceNotFoundException("Source family not found"));
        Family targetFamily = familyRepository.findById(targetFamilyId)
                .orElseThrow(() -> new ResourceNotFoundException("Target family not found"));
        
        if (!sourceFamily.getSchool().getId().equals(effectiveSchoolId) || 
            !targetFamily.getSchool().getId().equals(effectiveSchoolId)) {
            throw new ResourceNotFoundException("Family not found in this school");
        }

        // Move all students from source to target family
        List<Student> students = studentRepository.findByFamilyId(sourceFamilyId);
        students.forEach(student -> student.setFamily(targetFamily));
        studentRepository.saveAll(students);

        // Delete source family
        familyRepository.delete(sourceFamily);

        return toResponse(targetFamily);
    }

    @Transactional
    public void linkStudentToFamily(Long studentId, Long familyId, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Family not found"));
        
        if (!student.getSchool().getId().equals(effectiveSchoolId) || 
            !family.getSchool().getId().equals(effectiveSchoolId)) {
            throw new ResourceNotFoundException("Student or family not found in this school");
        }

        student.setFamily(family);
        studentRepository.save(student);
    }

    @Transactional
    public void unlinkStudentFromFamily(Long studentId, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        if (!student.getSchool().getId().equals(effectiveSchoolId)) {
            throw new ResourceNotFoundException("Student not found in this school");
        }

        student.setFamily(null);
        studentRepository.save(student);
    }

    public List<FamilyResponse> searchFamilies(String query, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        String searchQuery = query.toLowerCase();
        
        return familyRepository.findBySchoolId(effectiveSchoolId).stream()
                .filter(f -> f.getFamilyCode().toLowerCase().contains(searchQuery))
                .map(this::toResponse)
                .toList();
    }

    private FamilyResponse toResponse(Family family) {
        List<Student> members = studentRepository.findByFamilyId(family.getId());
        
        List<FamilyResponse.FamilyMemberResponse> memberResponses = members.stream()
                .map(student -> new FamilyResponse.FamilyMemberResponse(
                        student.getId(),
                        student.getName(),
                        student.getAdmissionNo(),
                        student.getGender(),
                        student.getStatus()
                ))
                .collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return new FamilyResponse(
                family.getId(),
                family.getSchool().getId(),
                family.getFamilyCode(),
                family.getStatus(),
                family.getCreatedAt() != null ? family.getCreatedAt().format(formatter) : null,
                family.getUpdatedAt() != null ? family.getUpdatedAt().format(formatter) : null,
                memberResponses
        );
    }

    private String generateFamilyCode() {
        return "FAM-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + 
               String.format("%04d", (int) (Math.random() * 10000));
    }
}
