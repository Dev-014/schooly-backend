package com.school.erp.service;

import com.school.erp.dto.academicyear.AcademicYearRequest;
import com.school.erp.dto.academicyear.AcademicYearResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.AcademicYear;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.AcademicYearRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public AcademicYearService(
            AcademicYearRepository academicYearRepository,
            SchoolRepository schoolRepository,
            AuthContextService authContextService
    ) {
        this.academicYearRepository = academicYearRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<AcademicYearResponse> getAllAcademicYears(Long schoolId) {
        return academicYearRepository.findBySchoolIdAndDeletedAtIsNull(authContextService.resolveSchoolId(schoolId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AcademicYearResponse getAcademicYearById(Long id, Long schoolId) {
        return toResponse(findAcademicYear(id, authContextService.resolveSchoolId(schoolId)));
    }

    public AcademicYearResponse createAcademicYear(AcademicYearRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = getSchool(effectiveSchoolId);
        AcademicYear academicYear = new AcademicYear();
        mapRequestToEntity(academicYear, request, school);
        return toResponse(academicYearRepository.save(academicYear));
    }

    public AcademicYearResponse updateAcademicYear(Long id, Long schoolId, AcademicYearRequest request) {
        authContextService.validateSameSchool(schoolId, request.schoolId());
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        AcademicYear academicYear = findAcademicYear(id, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);
        mapRequestToEntity(academicYear, request, school);
        return toResponse(academicYearRepository.save(academicYear));
    }

    public void deleteAcademicYear(Long id, Long schoolId) {
        AcademicYear academicYear = findAcademicYear(id, authContextService.resolveSchoolId(schoolId));
        academicYear.setDeletedAt(LocalDateTime.now());
        academicYearRepository.save(academicYear);
    }

    private AcademicYear findAcademicYear(Long id, Long schoolId) {
        return academicYearRepository.findByIdAndSchoolIdAndDeletedAtIsNull(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Academic year not found for id " + id + " and schoolId " + schoolId
                ));
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + schoolId));
    }

    private void mapRequestToEntity(AcademicYear academicYear, AcademicYearRequest request, School school) {
        academicYear.setName(request.name());
        academicYear.setStartDate(request.startDate());
        academicYear.setEndDate(request.endDate());
        if (request.status() != null) {
            academicYear.setStatus(request.status());
        }
        academicYear.setSchool(school);
    }

    private AcademicYearResponse toResponse(AcademicYear academicYear) {
        return new AcademicYearResponse(
                academicYear.getId(),
                academicYear.getName(),
                academicYear.getStartDate(),
                academicYear.getEndDate(),
                academicYear.getStatus(),
                academicYear.getSchool().getId()
        );
    }
}
