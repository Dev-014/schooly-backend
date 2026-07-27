package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.academic.*;
import com.school.erp.service.AcademicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/academics", "/api/v1/admin/academics"})
public class AcademicController {

    private final AcademicService academicService;

    public AcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    // --- Academic Years ---
    @GetMapping("/years")
    public ResponseEntity<ApiResponse<List<AcademicYearResponse>>> getAcademicYears(@RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getAcademicYears(schoolId),
                "Academic years fetched successfully"
        ));
    }

    @PostMapping("/years")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> createAcademicYear(@Valid @RequestBody AcademicYearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.createAcademicYear(request),
                "Academic year created successfully"
        ));
    }

    @PutMapping("/years/{id}/activate")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> activateAcademicYear(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.activateAcademicYear(id, schoolId),
                "Academic year activated successfully"
        ));
    }

    @PutMapping("/years/{id}")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> updateAcademicYear(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody AcademicYearRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.updateAcademicYear(id, schoolId, request),
                "Academic year updated successfully"
        ));
    }

    @DeleteMapping("/years/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAcademicYear(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        academicService.deleteAcademicYear(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Academic year deleted successfully"));
    }

    // --- Sections ---
    @GetMapping("/sections")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSections(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getSections(schoolId, classId),
                "Sections fetched successfully"
        ));
    }

    @PostMapping("/sections")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@Valid @RequestBody SectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.createSection(request),
                "Section created successfully"
        ));
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody SectionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.updateSection(id, schoolId, request),
                "Section updated successfully"
        ));
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        academicService.deleteSection(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Section deleted successfully"));
    }

    // --- Subjects ---
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> getSubjects(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String gradeLevel
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getSubjects(schoolId, gradeLevel),
                "Subjects fetched successfully"
        ));
    }

    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(@Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.createSubject(request),
                "Subject created successfully"
        ));
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody SubjectRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.updateSubject(id, schoolId, request),
                "Subject updated successfully"
        ));
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        academicService.deleteSubject(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subject deleted successfully"));
    }

    // --- Timetable Periods ---
    @GetMapping("/timetable/periods")
    public ResponseEntity<ApiResponse<List<TimetablePeriodResponse>>> getTimetablePeriods(@RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getTimetablePeriods(schoolId),
                "Timetable periods fetched successfully"
        ));
    }

    @PostMapping("/timetable/periods")
    public ResponseEntity<ApiResponse<TimetablePeriodResponse>> createTimetablePeriod(@Valid @RequestBody TimetablePeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.createTimetablePeriod(request),
                "Timetable period created successfully"
        ));
    }

    @PutMapping("/timetable/periods/{id}")
    public ResponseEntity<ApiResponse<TimetablePeriodResponse>> updateTimetablePeriod(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody TimetablePeriodRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.updateTimetablePeriod(id, schoolId, request),
                "Timetable period updated successfully"
        ));
    }

    @DeleteMapping("/timetable/periods/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimetablePeriod(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        academicService.deleteTimetablePeriod(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Timetable period deleted successfully"));
    }
    // --- Class Teacher Assignments ---
    @GetMapping("/class-teachers")
    public ResponseEntity<ApiResponse<List<ClassTeacherAssignmentResponse>>> getClassTeacherAssignments(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long academicYearId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getClassTeacherAssignments(schoolId, academicYearId),
                "Class teacher assignments fetched successfully"
        ));
    }

    @PostMapping("/class-teachers")
    public ResponseEntity<ApiResponse<ClassTeacherAssignmentResponse>> assignClassTeacher(
            @Valid @RequestBody ClassTeacherAssignmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.assignClassTeacher(request),
                "Class teacher assigned successfully"
        ));
    }

    @PutMapping("/class-teachers/{id}/status")
    public ResponseEntity<ApiResponse<ClassTeacherAssignmentResponse>> updateAssignmentStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.updateAssignmentStatus(id, schoolId, status),
                "Assignment status updated successfully"
        ));
    }

    // --- Class Subject Assignments (Level 2) ---
    @GetMapping("/class-subjects")
    public ResponseEntity<ApiResponse<List<ClassSubjectAssignmentResponse>>> getClassSubjectAssignments(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long academicYearId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getClassSubjectAssignments(schoolId, classId, sectionId, academicYearId),
                "Class subject assignments fetched successfully"
        ));
    }

    @PostMapping("/class-subjects")
    public ResponseEntity<ApiResponse<List<ClassSubjectAssignmentResponse>>> assignSubjectsToClass(
            @Valid @RequestBody ClassSubjectAssignmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.assignSubjectsToClass(request),
                "Subjects assigned to class successfully"
        ));
    }

    @DeleteMapping("/class-subjects/{id}")
    public ResponseEntity<ApiResponse<Void>> removeSubjectFromClass(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        academicService.removeSubjectFromClass(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subject removed from class"));
    }

    // --- Student Subject Enrollments (Level 3) ---
    @GetMapping("/student-enrollments")
    public ResponseEntity<ApiResponse<List<StudentSubjectEnrollmentResponse>>> getStudentEnrollments(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long academicYearId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getStudentEnrollments(schoolId, studentId, academicYearId),
                "Student enrollments fetched successfully"
        ));
    }

    @PostMapping("/student-enrollments")
    public ResponseEntity<ApiResponse<StudentSubjectEnrollmentResponse>> enrollStudentInSubject(
            @Valid @RequestBody StudentSubjectEnrollmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.enrollStudentInSubject(request),
                "Student enrolled in subject successfully"
        ));
    }

    @DeleteMapping("/student-enrollments/{id}")
    public ResponseEntity<ApiResponse<Void>> removeStudentEnrollment(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        academicService.removeStudentEnrollment(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student enrollment removed"));
    }

    // --- Timetable Entries (Weekly Grid & Daily Schedule) ---
    @GetMapping("/timetable-entries")
    public ResponseEntity<ApiResponse<List<TimetableEntryResponse>>> getTimetableGrid(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String dayOfWeek,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long teacherId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                academicService.getTimetableGrid(schoolId, classId, sectionId, dayOfWeek, academicYearId, teacherId),
                "Timetable grid fetched successfully"
        ));
    }

    @PostMapping("/timetable-entries/bulk")
    public ResponseEntity<ApiResponse<List<TimetableEntryResponse>>> saveTimetableGrid(
            @Valid @RequestBody BulkTimetableRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                academicService.saveTimetableGrid(request),
                "Timetable grid saved successfully"
        ));
    }

    @DeleteMapping("/timetable-entries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimetableEntry(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        academicService.deleteTimetableEntry(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Timetable entry deleted"));
    }
}


