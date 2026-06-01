package com.school.erp.service;

import com.school.erp.dto.schoolclass.SchoolClassRequest;
import com.school.erp.dto.schoolclass.SchoolClassResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolClass;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolClassRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public SchoolClassService(
            SchoolClassRepository schoolClassRepository,
            SchoolRepository schoolRepository,
            AuthContextService authContextService
    ) {
        this.schoolClassRepository = schoolClassRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<SchoolClassResponse> getAllClasses(Long schoolId) {
        return schoolClassRepository.findBySchoolId(authContextService.resolveSchoolId(schoolId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SchoolClassResponse getClassById(Long id, Long schoolId) {
        return toResponse(findClass(id, authContextService.resolveSchoolId(schoolId)));
    }

    public SchoolClassResponse createClass(SchoolClassRequest request) {
        SchoolClass schoolClass = new SchoolClass();
        School school = getSchool(authContextService.resolveSchoolId(request.schoolId()));
        mapRequestToEntity(schoolClass, request, school);
        return toResponse(schoolClassRepository.save(schoolClass));
    }

    public SchoolClassResponse updateClass(Long id, Long schoolId, SchoolClassRequest request) {
        authContextService.validateSameSchool(schoolId, request.schoolId());
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        SchoolClass schoolClass = findClass(id, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);
        mapRequestToEntity(schoolClass, request, school);
        return toResponse(schoolClassRepository.save(schoolClass));
    }

    public void deleteClass(Long id, Long schoolId) {
        SchoolClass schoolClass = findClass(id, authContextService.resolveSchoolId(schoolId));
        schoolClassRepository.delete(schoolClass);
    }

    private SchoolClass findClass(Long id, Long schoolId) {
        return schoolClassRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Class not found for id " + id + " and schoolId " + schoolId
                ));
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + schoolId));
    }

    private void mapRequestToEntity(SchoolClass schoolClass, SchoolClassRequest request, School school) {
        schoolClass.setName(request.name());
        schoolClass.setSchool(school);
    }

    private SchoolClassResponse toResponse(SchoolClass schoolClass) {
        return new SchoolClassResponse(
                schoolClass.getId(),
                schoolClass.getName(),
                schoolClass.getSchool().getId()
        );
    }
}
