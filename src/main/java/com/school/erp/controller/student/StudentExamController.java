package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.exam.ExamScheduleResponse;
import com.school.erp.dto.exam.AdmitCardPreviewResponse;
import com.school.erp.dto.exam.StudentReportCardResponse;
import com.school.erp.entity.student.Student;
import com.school.erp.exception.ResourceNotFoundException;
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
    private final ExamScheduleService examScheduleService;
    private final ExamAdmitCardService examAdmitCardService;
    private final ExamReportCardService examReportCardService;

    private Student getCurrentStudent(AuthenticatedUser user) {
        List<Student> students = studentRepository.findByUserId(user.userId());
        if (students.isEmpty()) {
            throw new ResourceNotFoundException("Student record not found for the logged-in user");
        }
        // If a user has multiple student records (e.g., siblings), we assume the first one for now
        // or we could require a studentId parameter if necessary.
        return students.get(0);
    }

    @GetMapping("/timetable")
    @PermissionRequired("student.exams.timetable.view")
    public ResponseEntity<ApiResponse<List<ExamScheduleResponse>>> getTimetable(
            @RequestParam(required = false) Long examSetupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Long schoolId = authContextService.resolveSchoolId(null);
        Student student = getCurrentStudent(currentUser);

        Long classId = student.getSchoolClass() != null ? student.getSchoolClass().getId() : null;
        Long sectionId = student.getSectionId(); // Depending on Student entity, might be getSection().getId() or getSectionId()

        Page<ExamScheduleResponse> result = examScheduleService.filterSchedules(
                schoolId, examSetupId, classId, sectionId, null, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Exam timetable retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/admit-card")
    @PermissionRequired("student.exams.admit_card.view")
    public ResponseEntity<ApiResponse<AdmitCardPreviewResponse>> getAdmitCard(
            @RequestParam Long examSetupId) {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Long schoolId = authContextService.resolveSchoolId(null);
        Student student = getCurrentStudent(currentUser);

        return ResponseEntity.ok(ApiResponse.success(
                examAdmitCardService.getPreview(schoolId, examSetupId, student.getId()),
                "Admit card retrieved successfully"));
    }

    @GetMapping("/results")
    @PermissionRequired("student.exams.results.view")
    public ResponseEntity<ApiResponse<List<StudentReportCardResponse>>> getResults() {

        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Long schoolId = authContextService.resolveSchoolId(null);
        Student student = getCurrentStudent(currentUser);

        return ResponseEntity.ok(ApiResponse.success(
                examReportCardService.getStudentReportCards(schoolId, student.getId()),
                "Report cards retrieved successfully"));
    }
}
