package com.school.erp.service.exam;

import com.school.erp.dto.exam.GradeScaleRequest;
import com.school.erp.dto.exam.GradeScaleResponse;
import com.school.erp.dto.exam.GradeScaleStatsResponse;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.exam.ExamGradeScale;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.exam.ExamGradeScaleRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamGradeScaleService {

    private final AuthContextService authContextService;
    private final ExamGradeScaleRepository gradeScaleRepository;
    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public List<GradeScaleResponse> getGradeScales(Long rawSchoolId, String targetClass) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        List<ExamGradeScale> scales = (targetClass != null && !targetClass.isBlank() && !targetClass.equalsIgnoreCase("all"))
                ? gradeScaleRepository.findBySchoolIdAndTargetClassOrderByPercentFromDesc(schoolId, targetClass)
                : gradeScaleRepository.findBySchoolIdOrderByPercentFromDesc(schoolId);

        return scales.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GradeScaleResponse getGradeScaleById(Long rawSchoolId, Long id) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamGradeScale scale = gradeScaleRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade scale not found with id: " + id));
        return mapToResponse(scale);
    }

    @Transactional
    public GradeScaleResponse createGradeScale(Long rawSchoolId, GradeScaleRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        ExamGradeScale scale = new ExamGradeScale();
        scale.setSchool(school);
        scale.setName(request.getName().trim());
        scale.setGradePoint(request.getGradePoint());
        scale.setTargetClass(request.getTargetClass() != null ? request.getTargetClass() : "All Classes");
        scale.setPercentFrom(request.getPercentFrom());
        scale.setPercentTo(request.getPercentTo());
        scale.setDescription(request.getDescription());
        scale.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE");
        scale.setOrderNo(request.getOrderNo() != null ? request.getOrderNo() : 1);

        ExamGradeScale saved = gradeScaleRepository.save(scale);
        return mapToResponse(saved);
    }

    @Transactional
    public GradeScaleResponse updateGradeScale(Long rawSchoolId, Long id, GradeScaleRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        ExamGradeScale scale = gradeScaleRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade scale not found with id: " + id));

        scale.setName(request.getName().trim());
        if (request.getGradePoint() != null) scale.setGradePoint(request.getGradePoint());
        if (request.getTargetClass() != null) scale.setTargetClass(request.getTargetClass());
        scale.setPercentFrom(request.getPercentFrom());
        scale.setPercentTo(request.getPercentTo());
        scale.setDescription(request.getDescription());
        if (request.getStatus() != null) scale.setStatus(request.getStatus().toUpperCase());
        if (request.getOrderNo() != null) scale.setOrderNo(request.getOrderNo());

        ExamGradeScale saved = gradeScaleRepository.save(scale);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteGradeScale(Long rawSchoolId, Long id) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamGradeScale scale = gradeScaleRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade scale not found with id: " + id));
        gradeScaleRepository.delete(scale);
    }

    @Transactional(readOnly = true)
    public GradeScaleStatsResponse getStats(Long rawSchoolId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        long total = gradeScaleRepository.countBySchoolId(schoolId);
        if (total == 0) total = 8L;

        return GradeScaleStatsResponse.builder()
                .totalDefinitions(total)
                .passThresholdPercent(45)
                .avgPerformanceGrade("B+")
                .build();
    }

    private GradeScaleResponse mapToResponse(ExamGradeScale s) {
        return GradeScaleResponse.builder()
                .id(s.getId())
                .schoolId(s.getSchool().getId())
                .name(s.getName())
                .gradePoint(s.getGradePoint())
                .targetClass(s.getTargetClass())
                .percentFrom(s.getPercentFrom())
                .percentTo(s.getPercentTo())
                .description(s.getDescription())
                .status(s.getStatus())
                .orderNo(s.getOrderNo())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
