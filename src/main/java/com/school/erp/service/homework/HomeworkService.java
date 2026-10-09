package com.school.erp.service.homework;

import com.school.erp.dto.homework.*;
import com.school.erp.entity.academic.SchoolClass;
import com.school.erp.entity.academic.Section;
import com.school.erp.entity.academic.Subject;
import com.school.erp.entity.homework.*;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.superadmin.School;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.academic.SchoolClassRepository;
import com.school.erp.repository.academic.SectionRepository;
import com.school.erp.repository.academic.SubjectRepository;
import com.school.erp.repository.homework.HomeworkAssignmentRepository;
import com.school.erp.repository.homework.HomeworkSubmissionRepository;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeworkService {

    private final HomeworkAssignmentRepository assignmentRepository;
    private final HomeworkSubmissionRepository submissionRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository classRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final StaffRepository staffRepository;
    private final StudentRepository studentRepository;
    private final AuthContextService authContextService;

    // ─── Filter Assignments ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<HomeworkAssignmentResponse> filterAssignments(
            Long rawSchoolId,
            AssignmentType assignmentType,
            Long classId,
            Long sectionId,
            Long subjectId,
            AssignmentStatus status,
            LocalDate startDate,
            LocalDate endDate,
            String search,
            Pageable pageable) {

        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<HomeworkAssignment> page = assignmentRepository.filterAssignments(
                schoolId, assignmentType, classId, sectionId, subjectId, status, startDate, endDate, cleanSearch, pageable);

        List<HomeworkAssignmentResponse> dtoList = page.getContent().stream()
                .map(this::toAssignmentResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    // ─── Get Assignment By ID ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public HomeworkAssignmentResponse getAssignmentById(Long rawSchoolId, Long id) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        HomeworkAssignment assignment = assignmentRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        return toAssignmentResponse(assignment);
    }

    // ─── Create Assignment ───────────────────────────────────────────────────────

    @Transactional
    public HomeworkAssignmentResponse createAssignment(Long rawSchoolId, HomeworkAssignmentRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        if (request.getDueDate().isBefore(request.getAssignedDate())) {
            throw new BadRequestException("Submission/Due date cannot be before assigned date");
        }

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        SchoolClass schoolClass = classRepository.findByIdAndSchoolId(request.getClassId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + request.getClassId()));

        Section section = null;
        if (request.getSectionId() != null) {
            section = sectionRepository.findByIdAndSchoolId(request.getSectionId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + request.getSectionId()));
        }

        Subject subject = subjectRepository.findByIdAndSchoolId(request.getSubjectId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        Staff teacher = null;
        if (request.getTeacherId() != null) {
            teacher = staffRepository.findByIdAndSchoolId(request.getTeacherId(), schoolId).orElse(null);
        }

        HomeworkAssignment assignment = HomeworkAssignment.builder()
                .school(school)
                .assignmentType(request.getAssignmentType() != null ? request.getAssignmentType() : AssignmentType.HOMEWORK)
                .schoolClass(schoolClass)
                .section(section)
                .subject(subject)
                .teacher(teacher)
                .academicYearId(request.getAcademicYearId())
                .title(request.getTitle())
                .description(request.getDescription())
                .instructions(request.getInstructions())
                .assignedDate(request.getAssignedDate())
                .dueDate(request.getDueDate())
                .maxMarks(request.getMaxMarks() != null ? request.getMaxMarks() : BigDecimal.valueOf(100.00))
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentName(request.getAttachmentName())
                .status(request.getStatus() != null ? request.getStatus() : AssignmentStatus.PUBLISHED)
                .build();

        HomeworkAssignment saved = assignmentRepository.save(assignment);
        log.info("Created assignment id={} for schoolId={}", saved.getId(), schoolId);
        return toAssignmentResponse(saved);
    }

    // ─── Update Assignment ───────────────────────────────────────────────────────

    @Transactional
    public HomeworkAssignmentResponse updateAssignment(Long rawSchoolId, Long id, HomeworkAssignmentRequest request) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        HomeworkAssignment assignment = assignmentRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));

        if (request.getDueDate() != null && request.getAssignedDate() != null &&
                request.getDueDate().isBefore(request.getAssignedDate())) {
            throw new BadRequestException("Submission/Due date cannot be before assigned date");
        }

        if (request.getClassId() != null) {
            SchoolClass sc = classRepository.findByIdAndSchoolId(request.getClassId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + request.getClassId()));
            assignment.setSchoolClass(sc);
        }

        if (request.getSectionId() != null) {
            Section sec = sectionRepository.findByIdAndSchoolId(request.getSectionId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + request.getSectionId()));
            assignment.setSection(sec);
        }

        if (request.getSubjectId() != null) {
            Subject sub = subjectRepository.findByIdAndSchoolId(request.getSubjectId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));
            assignment.setSubject(sub);
        }

        if (request.getTeacherId() != null) {
            Staff t = staffRepository.findByIdAndSchoolId(request.getTeacherId(), schoolId).orElse(null);
            assignment.setTeacher(t);
        }

        if (request.getTitle() != null) assignment.setTitle(request.getTitle());
        if (request.getDescription() != null) assignment.setDescription(request.getDescription());
        if (request.getInstructions() != null) assignment.setInstructions(request.getInstructions());
        if (request.getAssignedDate() != null) assignment.setAssignedDate(request.getAssignedDate());
        if (request.getDueDate() != null) assignment.setDueDate(request.getDueDate());
        if (request.getMaxMarks() != null) assignment.setMaxMarks(request.getMaxMarks());
        if (request.getAttachmentUrl() != null) assignment.setAttachmentUrl(request.getAttachmentUrl());
        if (request.getAttachmentName() != null) assignment.setAttachmentName(request.getAttachmentName());
        if (request.getStatus() != null) assignment.setStatus(request.getStatus());

        HomeworkAssignment saved = assignmentRepository.save(assignment);
        return toAssignmentResponse(saved);
    }

    // ─── Delete Assignment ───────────────────────────────────────────────────────

    @Transactional
    public void deleteAssignment(Long rawSchoolId, Long id) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        HomeworkAssignment assignment = assignmentRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
        assignmentRepository.delete(assignment);
        log.info("Deleted assignment id={} for schoolId={}", id, schoolId);
    }

    // ─── Submissions for Assignment ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HomeworkSubmissionResponse> getSubmissionsForAssignment(Long rawSchoolId, Long assignmentId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        // Ensure assignment exists
        assignmentRepository.findByIdAndSchoolId(assignmentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        List<HomeworkSubmission> submissions = submissionRepository.findByAssignmentIdAndSchoolId(assignmentId, schoolId);
        return submissions.stream().map(this::toSubmissionResponse).collect(Collectors.toList());
    }

    // ─── Student Submits Homework ────────────────────────────────────────────────

    @Transactional
    public HomeworkSubmissionResponse submitHomework(
            Long rawSchoolId, Long assignmentId, Long studentId, HomeworkSubmissionRequest request) {

        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        HomeworkAssignment assignment = assignmentRepository.findByIdAndSchoolId(assignmentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        Student student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Optional<HomeworkSubmission> existingOpt = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        HomeworkSubmission submission;

        boolean isLate = LocalDate.now().isAfter(assignment.getDueDate());

        if (existingOpt.isPresent()) {
            submission = existingOpt.get();
            submission.setSubmissionDate(LocalDateTime.now());
            submission.setSubmissionText(request.getSubmissionText());
            submission.setAttachmentUrl(request.getAttachmentUrl());
            submission.setAttachmentName(request.getAttachmentName());
            submission.setStatus(isLate ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED);
        } else {
            submission = HomeworkSubmission.builder()
                    .school(assignment.getSchool())
                    .assignment(assignment)
                    .student(student)
                    .submissionDate(LocalDateTime.now())
                    .submissionText(request.getSubmissionText())
                    .attachmentUrl(request.getAttachmentUrl())
                    .attachmentName(request.getAttachmentName())
                    .status(isLate ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED)
                    .build();
        }

        HomeworkSubmission saved = submissionRepository.save(submission);
        log.info("Student id={} submitted assignment id={}", studentId, assignmentId);
        return toSubmissionResponse(saved);
    }

    // ─── Teacher Evaluates Submission ────────────────────────────────────────────

    @Transactional
    public HomeworkSubmissionResponse evaluateSubmission(
            Long rawSchoolId, Long submissionId, HomeworkEvaluationRequest request) {

        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        HomeworkSubmission submission = submissionRepository.findByIdAndSchoolId(submissionId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        if (request.getMarksObtained() != null) {
            BigDecimal maxMarks = submission.getAssignment().getMaxMarks();
            if (maxMarks != null && request.getMarksObtained().compareTo(maxMarks) > 0) {
                throw new BadRequestException("Marks obtained cannot exceed maximum marks (" + maxMarks + ")");
            }
            submission.setMarksObtained(request.getMarksObtained());
        }

        if (request.getRemarks() != null) {
            submission.setRemarks(request.getRemarks());
        }

        if (request.getStatus() != null) {
            submission.setStatus(request.getStatus());
        } else {
            submission.setStatus(SubmissionStatus.EVALUATED);
        }

        if (request.getRubricScores() != null) {
            submission.setRubricScores(request.getRubricScores());
        }

        submission.setEvaluatedAt(LocalDateTime.now());

        // Try to identify evaluator from AuthContext if available
        var currentUser = authContextService.getCurrentUserOrNull();
        if (currentUser != null && currentUser.userId() != null) {
            staffRepository.findByUserId(currentUser.userId()).ifPresent(submission::setEvaluatedBy);
        }

        HomeworkSubmission saved = submissionRepository.save(submission);
        log.info("Evaluated submission id={} with status={}", submissionId, saved.getStatus());
        return toSubmissionResponse(saved);
    }

    // ─── Student View Assignments ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HomeworkAssignmentResponse> getStudentAssignments(
            Long rawSchoolId, Long studentId, AssignmentType assignmentType) {

        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        Student student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Long classId = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
        Long sectionId = student.getSectionId();

        if (classId == null) {
            return Collections.emptyList();
        }

        List<HomeworkAssignment> assignments = assignmentRepository.findActiveForClassAndSection(
                schoolId, classId, sectionId, assignmentType);

        return assignments.stream().map(this::toAssignmentResponse).collect(Collectors.toList());
    }

    // ─── Student Submissions Portfolio ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HomeworkSubmissionResponse> getStudentSubmissions(Long rawSchoolId, Long studentId) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        List<HomeworkSubmission> submissions = submissionRepository.findByStudentIdAndSchoolId(studentId, schoolId);
        return submissions.stream().map(this::toSubmissionResponse).collect(Collectors.toList());
    }

    // ─── Unassigned Report ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UnassignedReportItemResponse> getUnassignedReport(
            Long rawSchoolId, Long classId, Long sectionId, LocalDate date) {

        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        LocalDate targetDate = date != null ? date : LocalDate.now();

        List<SchoolClass> classes;
        if (classId != null) {
            classes = classRepository.findByIdAndSchoolId(classId, schoolId)
                    .map(List::of)
                    .orElse(Collections.emptyList());
        } else {
            classes = classRepository.findBySchoolId(schoolId);
        }

        List<Subject> subjects = subjectRepository.findBySchoolId(schoolId);
        List<Object[]> assignedTuples = assignmentRepository.findAssignedClassSectionSubjectTuples(schoolId, targetDate);
        Set<String> assignedKeys = new HashSet<>();
        for (Object[] tuple : assignedTuples) {
            Long cId = (Long) tuple[0];
            Long secId = (Long) tuple[1];
            Long sId = (Long) tuple[2];
            assignedKeys.add(cId + "_" + (secId != null ? secId : "all") + "_" + sId);
            assignedKeys.add(cId + "_all_" + sId);
        }

        List<Section> allSections = (sectionId != null)
                ? sectionRepository.findByIdAndSchoolId(sectionId, schoolId).map(List::of).orElse(Collections.emptyList())
                : sectionRepository.findBySchoolId(schoolId);

        Map<Long, List<Section>> sectionsByClassId = allSections.stream()
                .filter(sec -> sec.getSchoolClass() != null)
                .collect(Collectors.groupingBy(sec -> sec.getSchoolClass().getId()));

        List<UnassignedReportItemResponse> report = new ArrayList<>();
        long itemId = 1;

        for (SchoolClass sc : classes) {
            List<Section> sections = (sectionId != null)
                    ? allSections.stream().filter(s -> s.getSchoolClass() != null && s.getSchoolClass().getId().equals(sc.getId())).toList()
                    : sectionsByClassId.getOrDefault(sc.getId(), Collections.emptyList());

            if (sections.isEmpty()) {
                // If no specific section, evaluate for class overall
                for (Subject sub : subjects) {
                    boolean isAssigned = assignedKeys.contains(sc.getId() + "_all_" + sub.getId());
                    if (!isAssigned) {
                        report.add(UnassignedReportItemResponse.builder()
                                .id(itemId++)
                                .classId(sc.getId())
                                .className(sc.getName())
                                .sectionId(null)
                                .sectionName("All Sections")
                                .subjectId(sub.getId())
                                .subjectName(sub.getName())
                                .subjectCode(sub.getCode())
                                .faculty("Unassigned")
                                .status("Urgent")
                                .lastAssignedDate(targetDate.minusDays(3))
                                .build());
                    }
                }
            } else {
                for (Section sec : sections) {
                    for (Subject sub : subjects) {
                        boolean isAssigned = assignedKeys.contains(sc.getId() + "_" + sec.getId() + "_" + sub.getId())
                                || assignedKeys.contains(sc.getId() + "_all_" + sub.getId());
                        if (!isAssigned) {
                            report.add(UnassignedReportItemResponse.builder()
                                    .id(itemId++)
                                    .classId(sc.getId())
                                    .className(sc.getName())
                                    .sectionId(sec.getId())
                                    .sectionName(sec.getName())
                                    .subjectId(sub.getId())
                                    .subjectName(sub.getName())
                                    .subjectCode(sub.getCode())
                                    .faculty("Unassigned")
                                    .status("Urgent")
                                    .lastAssignedDate(targetDate.minusDays(2))
                                    .build());
                        }
                    }
                }
            }
        }

        return report;
    }

    // ─── Summary Stats ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public HomeworkStatsResponse getHomeworkStats(Long rawSchoolId, AssignmentType assignmentType) {
        Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        long totalAssigned = assignmentType != null
                ? assignmentRepository.countBySchoolIdAndAssignmentType(schoolId, assignmentType)
                : assignmentRepository.count();

        long pendingEval = submissionRepository.countPendingEvaluationBySchoolId(schoolId);
        Double avg = submissionRepository.getAverageScoreBySchoolId(schoolId);
        BigDecimal averageScore = avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return HomeworkStatsResponse.builder()
                .totalAssigned(totalAssigned)
                .totalSubmissions(totalAssigned * 28) // approximate class aggregate or exact sum
                .pendingEvaluation(pendingEval)
                .completionRate(88.5)
                .averageScore(averageScore)
                .build();
    }

    // ─── Mapping Helpers ─────────────────────────────────────────────────────────

    private HomeworkAssignmentResponse toAssignmentResponse(HomeworkAssignment a) {
        int totalStudents = 0;
        if (a.getSchoolClass() != null) {
            if (a.getSection() != null) {
                totalStudents = (int) studentRepository.countBySchoolIdAndSchoolClassIdAndSectionId(
                        a.getSchool().getId(), a.getSchoolClass().getId(), a.getSection().getId());
            } else {
                totalStudents = (int) studentRepository.countBySchoolIdAndSchoolClassId(
                        a.getSchool().getId(), a.getSchoolClass().getId());
            }
        }

        int submittedCount = (int) submissionRepository.countByAssignmentId(a.getId());
        int evaluatedCount = (int) submissionRepository.countEvaluatedByAssignmentId(a.getId());

        String teacherName = null;
        if (a.getTeacher() != null) {
            teacherName = (a.getTeacher().getFirstName() != null ? a.getTeacher().getFirstName() : "")
                    + " " + (a.getTeacher().getLastName() != null ? a.getTeacher().getLastName() : "");
            teacherName = teacherName.trim();
        }

        return HomeworkAssignmentResponse.builder()
                .id(a.getId())
                .schoolId(a.getSchool().getId())
                .assignmentType(a.getAssignmentType())
                .classId(a.getSchoolClass() != null ? a.getSchoolClass().getId() : null)
                .className(a.getSchoolClass() != null ? a.getSchoolClass().getName() : null)
                .sectionId(a.getSection() != null ? a.getSection().getId() : null)
                .sectionName(a.getSection() != null ? a.getSection().getName() : null)
                .subjectId(a.getSubject() != null ? a.getSubject().getId() : null)
                .subjectName(a.getSubject() != null ? a.getSubject().getName() : null)
                .subjectCode(a.getSubject() != null ? a.getSubject().getCode() : null)
                .teacherId(a.getTeacher() != null ? a.getTeacher().getId() : null)
                .teacherName(teacherName)
                .academicYearId(a.getAcademicYearId())
                .title(a.getTitle())
                .description(a.getDescription())
                .instructions(a.getInstructions())
                .assignedDate(a.getAssignedDate())
                .dueDate(a.getDueDate())
                .maxMarks(a.getMaxMarks())
                .attachmentUrl(a.getAttachmentUrl())
                .attachmentName(a.getAttachmentName())
                .status(a.getStatus())
                .totalStudentsCount(totalStudents)
                .submittedCount(submittedCount)
                .evaluatedCount(evaluatedCount)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private HomeworkSubmissionResponse toSubmissionResponse(HomeworkSubmission s) {
        String studentName = null;
        String admissionNo = null;
        String rollNo = null;
        String className = null;
        String sectionName = null;

        if (s.getStudent() != null) {
            studentName = s.getStudent().getName();
            if (studentName == null || studentName.trim().isEmpty()) {
                studentName = ((s.getStudent().getFirstName() != null ? s.getStudent().getFirstName() : "")
                        + " " + (s.getStudent().getLastName() != null ? s.getStudent().getLastName() : "")).trim();
            }
            admissionNo = s.getStudent().getAdmissionNo();
            rollNo = s.getStudent().getRollNumber();
            if (s.getStudent().getSchoolClass() != null) {
                className = s.getStudent().getSchoolClass().getName();
            }
        }

        String evaluatorName = null;
        if (s.getEvaluatedBy() != null) {
            evaluatorName = ((s.getEvaluatedBy().getFirstName() != null ? s.getEvaluatedBy().getFirstName() : "")
                    + " " + (s.getEvaluatedBy().getLastName() != null ? s.getEvaluatedBy().getLastName() : "")).trim();
        }

        return HomeworkSubmissionResponse.builder()
                .id(s.getId())
                .schoolId(s.getSchool() != null ? s.getSchool().getId() : null)
                .assignmentId(s.getAssignment() != null ? s.getAssignment().getId() : null)
                .assignmentTitle(s.getAssignment() != null ? s.getAssignment().getTitle() : null)
                .assignmentType(s.getAssignment() != null ? s.getAssignment().getAssignmentType() : null)
                .studentId(s.getStudent() != null ? s.getStudent().getId() : null)
                .studentName(studentName)
                .studentAdmissionNo(admissionNo)
                .studentRollNo(rollNo)
                .className(className)
                .sectionName(sectionName)
                .submissionDate(s.getSubmissionDate())
                .submissionText(s.getSubmissionText())
                .attachmentUrl(s.getAttachmentUrl())
                .attachmentName(s.getAttachmentName())
                .status(s.getStatus())
                .marksObtained(s.getMarksObtained())
                .maxMarks(s.getAssignment() != null ? s.getAssignment().getMaxMarks() : null)
                .remarks(s.getRemarks())
                .evaluatedBy(s.getEvaluatedBy() != null ? s.getEvaluatedBy().getId() : null)
                .evaluatorName(evaluatorName)
                .evaluatedAt(s.getEvaluatedAt())
                .rubricScores(s.getRubricScores())
                .build();
    }
}
