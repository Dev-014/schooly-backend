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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PermissionRequired({"student.exams.timetable.view", "academics.class_timetable.view"})
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
            @RequestParam Long examSetupId,
            @RequestParam(required = false) Long studentId) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Student student = resolveTargetStudent(currentUser, studentId);
        Long schoolId = student.getSchool() != null ? student.getSchool().getId() : authContextService.resolveSchoolId(null);

        return ResponseEntity.ok(ApiResponse.success(
                examAdmitCardService.getPreview(schoolId, examSetupId, student.getId()),
                "Admit card retrieved successfully"));
    }

    @GetMapping("/results")
    @PermissionRequired({"student.exams.results.view", "exams_results.report_card.view"})
    public ResponseEntity<ApiResponse<List<StudentReportCardResponse>>> getResults(
            @RequestParam(required = false) Long studentId) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Student student = resolveTargetStudent(currentUser, studentId);
        Long schoolId = student.getSchool() != null ? student.getSchool().getId() : authContextService.resolveSchoolId(null);

        return ResponseEntity.ok(ApiResponse.success(
                examReportCardService.getStudentReportCards(schoolId, student.getId()),
                "Report cards retrieved successfully"));
    }
}
