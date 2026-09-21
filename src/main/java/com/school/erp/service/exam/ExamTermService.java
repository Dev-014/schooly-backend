package com.school.erp.service.exam;

import com.school.erp.dto.exam.ExamTermRequest;
import com.school.erp.dto.exam.ExamTermResponse;
import com.school.erp.dto.exam.ExamTermStatsResponse;
import com.school.erp.entity.academic.AcademicYear;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.exam.ExamTerm;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.academic.AcademicYearRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.exam.ExamTermRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamTermService {

    private final AuthContextService authContextService;
    private final ExamTermRepository examTermRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;

    @Transactional(readOnly = true)
    public List<ExamTermResponse> getTerms(Long rawSchoolId, Long academicYearId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        List<ExamTerm> terms = (academicYearId != null)
                ? examTermRepository.findBySchoolIdAndAcademicYearIdOrderByStartDateDesc(schoolId, academicYearId)
                : examTermRepository.findBySchoolIdOrderByStartDateDesc(schoolId);

        return terms.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExamTermResponse getTermById(Long rawSchoolId, Long id) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamTerm term = examTermRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam term not found with id: " + id));
        return mapToResponse(term);
    }

    @Transactional
    public ExamTermResponse createTerm(Long rawSchoolId, ExamTermRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Term end date cannot be before start date");
        }

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        AcademicYear academicYear = null;
        if (request.getAcademicYearId() != null) {
            academicYear = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + request.getAcademicYearId()));
        }

        ExamTerm term = new ExamTerm();
        term.setSchool(school);
        term.setAcademicYear(academicYear);
        term.setName(request.getName().trim());
        term.setDescription(request.getDescription());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        term.setResultPublishDate(request.getResultPublishDate());
        term.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE");

        ExamTerm saved = examTermRepository.save(term);
        return mapToResponse(saved);
    }

    @Transactional
    public ExamTermResponse updateTerm(Long rawSchoolId, Long id, ExamTermRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Term end date cannot be before start date");
        }

        ExamTerm term = examTermRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam term not found with id: " + id));

        if (request.getAcademicYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + request.getAcademicYearId()));
            term.setAcademicYear(academicYear);
        }

        term.setName(request.getName().trim());
        term.setDescription(request.getDescription());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());
        term.setResultPublishDate(request.getResultPublishDate());
        if (request.getStatus() != null) {
            term.setStatus(request.getStatus().toUpperCase());
        }

        ExamTerm saved = examTermRepository.save(term);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteTerm(Long rawSchoolId, Long id) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        ExamTerm term = examTermRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam term not found with id: " + id));

        long childExams = examTermRepository.countExamsByTermId(id);
        if (childExams > 0) {
            throw new BadRequestException("Cannot delete term because it has " + childExams + " linked examinations. Remove examinations first.");
        }

        examTermRepository.delete(term);
    }

    @Transactional(readOnly = true)
    public ExamTermStatsResponse getStats(Long rawSchoolId, Long academicYearId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        long total = examTermRepository.countBySchoolId(schoolId);
        long active = examTermRepository.countBySchoolIdAndStatus(schoolId, "ACTIVE");
        long scheduled = examTermRepository.countBySchoolIdAndStatus(schoolId, "SCHEDULED");
        long completed = examTermRepository.countBySchoolIdAndStatus(schoolId, "COMPLETED");

        String sessionName = "Current Academic Session";
        if (academicYearId != null) {
            sessionName = academicYearRepository.findById(academicYearId)
                    .map(AcademicYear::getDisplayName)
                    .orElse(sessionName);
        } else {
            sessionName = academicYearRepository.findBySchoolId(schoolId).stream()
                    .filter(y -> "ACTIVE".equalsIgnoreCase(y.getStatus()))
                    .map(AcademicYear::getDisplayName)
                    .findFirst()
                    .orElse("Academic Session 2024-25");
        }

        return ExamTermStatsResponse.builder()
                .totalTerms(total)
                .activeTerms(active)
                .scheduledTerms(scheduled)
                .completedTerms(completed)
                .currentAcademicSession(sessionName)
                .systemHealthStatus("System Healthy")
                .build();
    }

    private ExamTermResponse mapToResponse(ExamTerm term) {
        long examCount = examTermRepository.countExamsByTermId(term.getId());
        return ExamTermResponse.builder()
                .id(term.getId())
                .schoolId(term.getSchool().getId())
                .academicYearId(term.getAcademicYear() != null ? term.getAcademicYear().getId() : null)
                .academicYearName(term.getAcademicYear() != null ? term.getAcademicYear().getDisplayName() : null)
                .name(term.getName())
                .description(term.getDescription())
                .startDate(term.getStartDate())
                .endDate(term.getEndDate())
                .resultPublishDate(term.getResultPublishDate())
                .status(term.getStatus())
                .examCount(examCount)
                .createdAt(term.getCreatedAt())
                .updatedAt(term.getUpdatedAt())
                .build();
    }
}
