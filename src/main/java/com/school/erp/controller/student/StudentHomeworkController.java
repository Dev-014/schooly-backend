package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.homework.HomeworkAssignmentResponse;
import com.school.erp.dto.homework.HomeworkSubmissionRequest;
import com.school.erp.dto.homework.HomeworkSubmissionResponse;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.homework.AssignmentType;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentParent;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.exception.UnauthorizedException;
import com.school.erp.repository.student.StudentParentRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.homework.HomeworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/student/homework", "/api/student/homework"})
@RequiredArgsConstructor
public class StudentHomeworkController {

    private final AuthContextService authContextService;
    private final HomeworkService homeworkService;
    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;

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

    // ─── Get Active Assignments for Student's Class & Section ──────────────────

    @GetMapping
    @PermissionRequired("homework.assignment.view")
    public ResponseEntity<ApiResponse<List<HomeworkAssignmentResponse>>> getStudentHomework(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) AssignmentType type,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String status) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Student student = resolveTargetStudent(currentUser, studentId);
        Long effectiveSchoolId = student.getSchool() != null ? student.getSchool().getId() : authContextService.resolveSchoolId(schoolId);

        List<HomeworkAssignmentResponse> assignments = homeworkService.getStudentAssignments(
                effectiveSchoolId, student.getId(), type);

        if (subjectId != null) {
            assignments = assignments.stream()
                    .filter(a -> a.getSubjectId() != null && a.getSubjectId().equals(subjectId))
                    .toList();
        }

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            assignments = assignments.stream()
                    .filter(a -> status.equalsIgnoreCase(a.getSubmissionStatus()) ||
                            (a.getStatus() != null && status.equalsIgnoreCase(a.getStatus().name())))
                    .toList();
        }

        return ResponseEntity.ok(ApiResponse.success(
                assignments,
                "Student assignments retrieved successfully"));
    }

    // ─── Submit Homework / Classwork ─────────────────────────────────────────────

    @PostMapping("/{assignmentId}/submit")
    @PermissionRequired("homework.submission.edit")
    public ResponseEntity<ApiResponse<HomeworkSubmissionResponse>> submitHomework(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long assignmentId,
            @RequestParam(required = false) Long studentId,
            @RequestBody HomeworkSubmissionRequest request) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Student student = resolveTargetStudent(currentUser, studentId);
        Long effectiveSchoolId = student.getSchool() != null ? student.getSchool().getId() : authContextService.resolveSchoolId(schoolId);

        HomeworkSubmissionResponse response = homeworkService.submitHomework(
                effectiveSchoolId, assignmentId, student.getId(), request);

        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Homework submitted successfully"));
    }

    // ─── View Student Portfolio / Submitted Work ─────────────────────────────────

    @GetMapping("/portfolio")
    @PermissionRequired("homework.assignment.view")
    public ResponseEntity<ApiResponse<List<HomeworkSubmissionResponse>>> getStudentPortfolio(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long studentId) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Student student = resolveTargetStudent(currentUser, studentId);
        Long effectiveSchoolId = student.getSchool() != null ? student.getSchool().getId() : authContextService.resolveSchoolId(schoolId);

        List<HomeworkSubmissionResponse> submissions = homeworkService.getStudentSubmissions(
                effectiveSchoolId, student.getId());

        return ResponseEntity.ok(ApiResponse.success(
                submissions,
                "Student portfolio retrieved successfully"));
    }
}
