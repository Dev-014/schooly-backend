package com.school.erp.service;

import com.school.erp.dto.section.SectionRequest;
import com.school.erp.dto.section.SectionResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.Section;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.SectionRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public SectionService(
            SectionRepository sectionRepository,
            SchoolRepository schoolRepository,
            AuthContextService authContextService
    ) {
        this.sectionRepository = sectionRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<SectionResponse> getAllSections(Long schoolId) {
        return sectionRepository.findBySchoolIdAndDeletedAtIsNull(authContextService.resolveSchoolId(schoolId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SectionResponse getSectionById(Long id, Long schoolId) {
        return toResponse(findSection(id, authContextService.resolveSchoolId(schoolId)));
    }

    public SectionResponse createSection(SectionRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = getSchool(effectiveSchoolId);
        Section section = new Section();
        mapRequestToEntity(section, request, school);
        return toResponse(sectionRepository.save(section));
    }

    public SectionResponse updateSection(Long id, Long schoolId, SectionRequest request) {
        authContextService.validateSameSchool(schoolId, request.schoolId());
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        Section section = findSection(id, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);
        mapRequestToEntity(section, request, school);
        return toResponse(sectionRepository.save(section));
    }

    public void deleteSection(Long id, Long schoolId) {
        Section section = findSection(id, authContextService.resolveSchoolId(schoolId));
        section.setDeletedAt(LocalDateTime.now());
        sectionRepository.save(section);
    }

    private Section findSection(Long id, Long schoolId) {
        return sectionRepository.findByIdAndSchoolIdAndDeletedAtIsNull(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Section not found for id " + id + " and schoolId " + schoolId
                ));
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + schoolId));
    }

    private void mapRequestToEntity(Section section, SectionRequest request, School school) {
        section.setName(request.name());
        section.setSchool(school);
    }

    private SectionResponse toResponse(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getName(),
                section.getSchool().getId()
        );
    }
}
