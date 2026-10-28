package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.exam.ExamScheduleResponse;
import com.school.erp.dto.exam.AdmitCardPreviewResponse;
import com.school.erp.dto.exam.StudentReportCardResponse;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentParent;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.exception.UnauthorizedException;
import com.school.erp.repository.student.StudentParentRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.exam.ExamScheduleService;
import com.school.erp.service.exam.ExamAdmitCardService;
import com.school.erp.service.exam.ExamReportCardService;
import com.school.erp.dto.exam.EligibleTermExamsResponse;
import com.school.erp.entity.exam.ExamSetup;
import com.school.erp.entity.exam.ExamTerm;
import com.school.erp.repository.exam.ExamApplicabilityRepository;
import com.school.erp.repository.exam.ExamScheduleRepository;
import com.school.erp.repository.exam.ExamSetupRepository;
import com.school.erp.repository.exam.ExamTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student/exams")
@RequiredArgsConstructor
public class StudentExamController {

    private final AuthContextService authContextService;
    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;
    private final ExamScheduleService examScheduleService;
    private final ExamAdmitCardService examAdmitCardService;
    private final ExamReportCardService examReportCardService;
    private final ExamTermRepository examTermRepository;
    private final ExamSetupRepository examSetupRepository;
    private final ExamApplicabilityRepository examApplicabilityRepository;
    private final ExamScheduleRepository examScheduleRepository;

    private Student resolveTargetStudent(AuthenticatedUser user, Long requestedStudentId) {
        if (user.role() == UserRole.PARENT) {
            List<StudentParent> relations = studentParentRepository.findByIdParentUserId(user.userId());
            if (relations.isEmpty()) {
                throw new ResourceNotFoundException("No children linked to parent profile");
            }
            if (requestedStudentId != null) {
                return relations.stream()
                        .filter(sp -> sp.getStudent().getId().equals(requestedStudentId))
                        .map(StudentParent::getStudent)
                        .findFirst()
                        .orElseThrow(() -> new UnauthorizedException("You do not have access to this student's records"));
            }
            return relations.get(0).getStudent();
        }

        if (user.role() == UserRole.STUDENT) {
            List<Student> students = studentRepository.findByUserId(user.userId());
            if (students.isEmpty()) {
                throw new ResourceNotFoundException("Student record not found for the logged-in user");
            }
            return students.get(0);
        }

        // Staff / Admin impersonation or viewing
        if (requestedStudentId != null) {
            return studentRepository.findById(requestedStudentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        }

        throw new IllegalArgumentException("studentId parameter is required for staff or admin roles");
    }

    @GetMapping("/timetable")
    @PermissionRequired({
            "student.exams.timetable.view",
            "academics.class_timetable.view",
            "exams_results.exam_schedule_student.view"
    })
    public ResponseEntity<ApiResponse<List<ExamScheduleResponse>>> getTimetable(
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Student student = resolveTargetStudent(currentUser, studentId);
        Long schoolId = student.getSchool() != null ? student.getSchool().getId() : authContextService.resolveSchoolId(null);

        Long classId = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
        Long sectionId = student.getSectionId();

        Page<ExamScheduleResponse> result = examScheduleService.filterSchedules(
                schoolId, examSetupId, classId, sectionId, null, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Exam timetable retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/admit-card")
    @PermissionRequired({"student.exams.admit_card.view", "exams_results.exam_schedule_student.view"})
    public ResponseEntity<ApiResponse<AdmitCardPreviewResponse>> getAdmitCard(
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(required = false) Long studentId) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Student student = resolveTargetStudent(currentUser, studentId);
        Long schoolId = student.getSchool() != null ? student.getSchool().getId() : authContextService.resolveSchoolId(null);

        Long resolvedExamSetupId = examSetupId != null
                ? examSetupId
                : examAdmitCardService.resolveDefaultExamSetupId(schoolId, student);

        AdmitCardPreviewResponse preview = examAdmitCardService.getPreview(schoolId, resolvedExamSetupId, student.getId());
        boolean issued = "GENERATED".equals(preview.getStatus()) || "RELEASED".equals(preview.getStatus());
        if (!issued) {
            preview.setCardNumber(null);
            preview.setSchedule(List.of());
        }
        return ResponseEntity.ok(ApiResponse.success(
                preview,
                issued ? "Admit card retrieved successfully"
                        : "Admit card is not generated yet for this examination"));
    }

    @GetMapping("/eligible-exams")
    @PermissionRequired({
            "student.exams.timetable.view",
            "exams_results.student_exams.view_timetable",
            "student.exams.admit_card.view",
            "student.exams.results.view",
            "exams_results.exam_schedule_student.view"
    })
    public ResponseEntity<ApiResponse<List<EligibleTermExamsResponse>>> getEligibleExams(
            @RequestParam(required = false) Long studentId) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Student student = resolveTargetStudent(currentUser, studentId);
        Long schoolId = student.getSchool() != null ? student.getSchool().getId() : authContextService.resolveSchoolId(null);

        Long classId = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
        Long sectionId = student.getSectionId();

        List<ExamTerm> terms = examTermRepository.findBySchoolIdOrderByStartDateDesc(schoolId);
        List<EligibleTermExamsResponse> result = new ArrayList<>();

        for (ExamTerm term : terms) {
            List<ExamSetup> setups = examSetupRepository.findBySchoolIdAndTermIdOrderByOrderNoAsc(schoolId, term.getId());
            List<EligibleTermExamsResponse.EligibleExamOption> eligibleExams = new ArrayList<>();

            for (ExamSetup setup : setups) {
                boolean isEligible = true;

                if (classId != null) {
                    int applicabilityCount = examApplicabilityRepository.countBySchoolIdAndExamSetupId(schoolId, setup.getId());
                    if (applicabilityCount > 0) {
                        isEligible = examApplicabilityRepository.findBySchoolIdAndExamSetupId(schoolId, setup.getId()).stream()
                                .anyMatch(app -> app.getSchoolClass().getId().equals(classId)
                                        && (app.getSection() == null || app.getSection().getId().equals(sectionId)));
                    } else {
                        boolean hasClassSchedule = !examScheduleRepository
                                .findBySchoolIdAndExamSetupIdAndSchoolClassIdOrderByExamDateAscStartTimeAsc(schoolId, setup.getId(), classId)
                                .isEmpty();
                        long totalSchedules = examSetupRepository.countSchedulesByExamSetupId(setup.getId());
                        if (totalSchedules > 0 && !hasClassSchedule) {
                            isEligible = false;
                        }
                    }
                }

                if (isEligible) {
                    eligibleExams.add(EligibleTermExamsResponse.EligibleExamOption.builder()
                            .id(setup.getId())
                            .name(setup.getName())
                            .groupName(setup.getGroupName())
                            .evaluationType(setup.getEvaluationType())
                            .status(setup.getStatus())
                            .orderNo(setup.getOrderNo())
                            .build());
                }
            }

            boolean isCurrent = "ACTIVE".equalsIgnoreCase(term.getStatus())
                    || (term.getStartDate() != null && term.getEndDate() != null
                    && !java.time.LocalDate.now().isBefore(term.getStartDate())
                    && !java.time.LocalDate.now().isAfter(term.getEndDate()));

            result.add(EligibleTermExamsResponse.builder()
                    .termId(term.getId())
                    .termName(term.getName())
                    .startDate(term.getStartDate())
                    .endDate(term.getEndDate())
                    .isCurrent(isCurrent)
                    .exams(eligibleExams)
                    .build());
        }

        return ResponseEntity.ok(ApiResponse.success(result, "Eligible terms and examinations retrieved successfully"));
    }

    @GetMapping("/results")
    @PermissionRequired({"student.exams.results.view", "exams_results.report_card.view", "exams_results.exam_schedule_student.view"})
    public ResponseEntity<ApiResponse<List<StudentReportCardResponse>>> getResults(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) Long examSetupId) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Student student = resolveTargetStudent(currentUser, studentId);
        Long schoolId = student.getSchool() != null ? student.getSchool().getId() : authContextService.resolveSchoolId(null);

        return ResponseEntity.ok(ApiResponse.success(
                examReportCardService.getStudentReportCards(schoolId, student.getId(), academicYearId, termId, examSetupId),
                "Report cards retrieved successfully"));
    }
}
