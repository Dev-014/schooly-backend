package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.homework.HomeworkAssignmentResponse;
import com.school.erp.dto.homework.HomeworkSubmissionRequest;
import com.school.erp.dto.homework.HomeworkSubmissionResponse;
import com.school.erp.entity.homework.AssignmentType;
import com.school.erp.entity.student.Student;
import com.school.erp.exception.BadRequestException;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.homework.HomeworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/student/homework", "/api/student/homework"})
@RequiredArgsConstructor
public class StudentHomeworkController {

    private final HomeworkService homeworkService;
    private final StudentRepository studentRepository;

    // ─── Get Active Assignments for Student's Class & Section ──────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<HomeworkAssignmentResponse>>> getStudentHomework(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) AssignmentType type) {

        Long effectiveStudentId = resolveEffectiveStudentId(studentId, schoolId);
        List<HomeworkAssignmentResponse> assignments = homeworkService.getStudentAssignments(
                schoolId, effectiveStudentId, type);

        return ResponseEntity.ok(ApiResponse.success(
                assignments,
                "Student assignments retrieved successfully"));
    }

    // ─── Submit Homework / Classwork ─────────────────────────────────────────────

    @PostMapping("/{assignmentId}/submit")
    public ResponseEntity<ApiResponse<HomeworkSubmissionResponse>> submitHomework(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long assignmentId,
            @RequestParam(required = false) Long studentId,
            @RequestBody HomeworkSubmissionRequest request) {

        Long effectiveStudentId = resolveEffectiveStudentId(studentId, schoolId);
        HomeworkSubmissionResponse response = homeworkService.submitHomework(
                schoolId, assignmentId, effectiveStudentId, request);

        return ResponseEntity.ok(ApiResponse.success(
                response,
                "Homework submitted successfully"));
    }

    // ─── View Student Portfolio / Submitted Work ─────────────────────────────────

    @GetMapping("/portfolio")
    public ResponseEntity<ApiResponse<List<HomeworkSubmissionResponse>>> getStudentPortfolio(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long studentId) {

        Long effectiveStudentId = resolveEffectiveStudentId(studentId, schoolId);
        List<HomeworkSubmissionResponse> submissions = homeworkService.getStudentSubmissions(
                schoolId, effectiveStudentId);

        return ResponseEntity.ok(ApiResponse.success(
                submissions,
                "Student portfolio retrieved successfully"));
    }

    // ─── Helper: Resolve Student ID from Auth Token or Query ─────────────────────

    private Long resolveEffectiveStudentId(Long requestedStudentId, Long schoolId) {
        if (requestedStudentId != null) {
            return requestedStudentId;
        }

        AuthenticatedUser authUser = AuthContextHolder.get();
        if (authUser != null && authUser.userId() != null) {
            List<Student> students = studentRepository.findByUserId(authUser.userId());
            if (!students.isEmpty()) {
                return students.get(0).getId();
            }
        }

        // Fallback: pick first active student for school for demo/development if exists
        Long targetSchool = schoolId != null ? schoolId : 1L;
        List<Student> students = studentRepository.findBySchoolId(targetSchool);
        if (!students.isEmpty()) {
            return students.get(0).getId();
        }

        throw new BadRequestException("studentId is required or student context not found");
    }
}
