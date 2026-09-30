package com.school.erp.service.frontoffice;

import com.school.erp.dto.frontoffice.EntranceExamRequest;
import com.school.erp.dto.frontoffice.EntranceExamResponse;
import com.school.erp.dto.frontoffice.EntranceExamStatsResponse;
import com.school.erp.entity.frontoffice.SchoolEntranceExam;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.frontoffice.SchoolEntranceExamRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolEntranceExamService {

    private final AuthContextService authContextService;
    private final SchoolEntranceExamRepository entranceExamRepository;
    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public Page<EntranceExamResponse> filterEntranceExams(
            Long rawSchoolId,
            String search,
            String className,
            String status,
            Pageable pageable) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanClass = (className != null && !className.isBlank() && !className.equalsIgnoreCase("all")) ? className.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<SchoolEntranceExam> page = entranceExamRepository.filterEntranceExams(
                schoolId, cleanSearch, cleanClass, cleanStatus, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public EntranceExamResponse getEntranceExamById(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        SchoolEntranceExam exam = entranceExamRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrance exam candidate not found with id: " + id));
        return mapToResponse(exam);
    }

    @Transactional
    public EntranceExamResponse createEntranceExam(Long rawSchoolId, EntranceExamRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        SchoolEntranceExam exam = new SchoolEntranceExam();
        exam.setSchool(school);
        exam.setCandidateName(request.getCandidateName());
        exam.setMobileNumber(request.getMobileNumber());
        exam.setParentName(request.getParentName());
        exam.setGender(request.getGender());
        exam.setClassName(request.getClassName());
        exam.setExamName(request.getExamName() != null && !request.getExamName().isBlank() ? request.getExamName() : "Entrance Exam");
        exam.setCenterName(request.getCenterName() != null && !request.getCenterName().isBlank() ? request.getCenterName() : "Main Campus");
        exam.setExamDate(request.getExamDate() != null ? request.getExamDate() : LocalDate.now());
        exam.setExamTime(request.getExamTime());
        exam.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "Scheduled");
        exam.setScore(request.getScore());
        exam.setNotes(request.getNotes());

        SchoolEntranceExam saved = entranceExamRepository.save(exam);
        return mapToResponse(saved);
    }

    @Transactional
    public EntranceExamResponse updateEntranceExam(Long rawSchoolId, Long id, EntranceExamRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        SchoolEntranceExam exam = entranceExamRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrance exam candidate not found with id: " + id));

        if (request.getCandidateName() != null && !request.getCandidateName().isBlank()) {
            exam.setCandidateName(request.getCandidateName());
        }
        exam.setMobileNumber(request.getMobileNumber());
        exam.setParentName(request.getParentName());
        exam.setGender(request.getGender());
        exam.setClassName(request.getClassName());
        if (request.getExamName() != null) exam.setExamName(request.getExamName());
        if (request.getCenterName() != null) exam.setCenterName(request.getCenterName());
        if (request.getExamDate() != null) exam.setExamDate(request.getExamDate());
        if (request.getExamTime() != null) exam.setExamTime(request.getExamTime());
        if (request.getStatus() != null && !request.getStatus().isBlank()) exam.setStatus(request.getStatus());
        if (request.getScore() != null) exam.setScore(request.getScore());
        exam.setNotes(request.getNotes());

        SchoolEntranceExam saved = entranceExamRepository.save(exam);
        return mapToResponse(saved);
    }

    @Transactional
    public EntranceExamResponse updateStatus(Long rawSchoolId, Long id, String status) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        SchoolEntranceExam exam = entranceExamRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrance exam candidate not found with id: " + id));

        exam.setStatus(status);
        SchoolEntranceExam saved = entranceExamRepository.save(exam);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteEntranceExam(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        SchoolEntranceExam exam = entranceExamRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrance exam candidate not found with id: " + id));
        entranceExamRepository.delete(exam);
    }

    @Transactional(readOnly = true)
    public EntranceExamStatsResponse getStats(Long rawSchoolId) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        long totalCandidates = entranceExamRepository.countBySchoolId(schoolId);
        List<LocalDate> upcomingDates = entranceExamRepository.findUpcomingExamDates(
                schoolId, LocalDate.now(), PageRequest.of(0, 1));
        LocalDate nextDate = upcomingDates.isEmpty() ? null : upcomingDates.get(0);

        return EntranceExamStatsResponse.builder()
                .totalCandidates(totalCandidates)
                .nextExamBatchDate(nextDate)
                .build();
    }

    private EntranceExamResponse mapToResponse(SchoolEntranceExam e) {
        return EntranceExamResponse.builder()
                .id(e.getId())
                .schoolId(e.getSchool().getId())
                .candidateName(e.getCandidateName())
                .mobileNumber(e.getMobileNumber())
                .parentName(e.getParentName())
                .gender(e.getGender())
                .className(e.getClassName())
                .examName(e.getExamName())
                .centerName(e.getCenterName())
                .examDate(e.getExamDate())
                .examTime(e.getExamTime())
                .status(e.getStatus())
                .score(e.getScore())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
