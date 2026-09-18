package com.school.erp.service.exam;

import com.school.erp.dto.exam.DivisionRequest;
import com.school.erp.dto.exam.DivisionResponse;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.exam.ExamDivision;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.exam.ExamDivisionRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamDivisionService {

    private final AuthContextService authContextService;
    private final ExamDivisionRepository divisionRepository;
    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public List<DivisionResponse> getDivisions(Long rawSchoolId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        List<ExamDivision> divisions = divisionRepository.findBySchoolIdOrderByPercentFromDesc(schoolId);
        return divisions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DivisionResponse getDivisionById(Long rawSchoolId, Long id) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamDivision division = divisionRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with id: " + id));
        return mapToResponse(division);
    }

    @Transactional
    public DivisionResponse createDivision(Long rawSchoolId, DivisionRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        ExamDivision division = new ExamDivision();
        division.setSchool(school);
        division.setName(request.getName().trim());
        division.setPercentFrom(request.getPercentFrom());
        division.setPercentTo(request.getPercentTo());
        division.setDescription(request.getDescription());
        division.setColorTag(request.getColorTag() != null ? request.getColorTag().toUpperCase() : "BLUE");
        division.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE");
        division.setOrderNo(request.getOrderNo() != null ? request.getOrderNo() : 1);

        ExamDivision saved = divisionRepository.save(division);
        return mapToResponse(saved);
    }

    @Transactional
    public DivisionResponse updateDivision(Long rawSchoolId, Long id, DivisionRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        ExamDivision division = divisionRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with id: " + id));

        division.setName(request.getName().trim());
        division.setPercentFrom(request.getPercentFrom());
        division.setPercentTo(request.getPercentTo());
        division.setDescription(request.getDescription());
        if (request.getColorTag() != null) division.setColorTag(request.getColorTag().toUpperCase());
        if (request.getStatus() != null) division.setStatus(request.getStatus().toUpperCase());
        if (request.getOrderNo() != null) division.setOrderNo(request.getOrderNo());

        ExamDivision saved = divisionRepository.save(division);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteDivision(Long rawSchoolId, Long id) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamDivision division = divisionRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with id: " + id));
        divisionRepository.delete(division);
    }

    private DivisionResponse mapToResponse(ExamDivision d) {
        return DivisionResponse.builder()
                .id(d.getId())
                .schoolId(d.getSchool().getId())
                .name(d.getName())
                .percentFrom(d.getPercentFrom())
                .percentTo(d.getPercentTo())
                .description(d.getDescription())
                .colorTag(d.getColorTag())
                .status(d.getStatus())
                .orderNo(d.getOrderNo())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
