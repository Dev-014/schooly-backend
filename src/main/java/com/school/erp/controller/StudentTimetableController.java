package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.StudentTimetableResponse;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.StudentTimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/student/timetable", "/api/student/timetable"})
@RequiredArgsConstructor
public class StudentTimetableController {

    private final StudentTimetableService studentTimetableService;

    @GetMapping
    public ResponseEntity<ApiResponse<StudentTimetableResponse>> getTimetable(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String dayOfWeek
    ) {
        AuthenticatedUser authUser = AuthContextHolder.get();
        Long effectiveUserId = authUser != null ? authUser.userId() : null;
        Long effectiveSchoolId = schoolId != null ? schoolId : (authUser != null ? authUser.schoolId() : null);

        StudentTimetableResponse response = studentTimetableService.getStudentTimetable(studentId, effectiveSchoolId, effectiveUserId, dayOfWeek);
        return ResponseEntity.ok(ApiResponse.success(response, "Student timetable fetched successfully"));
    }
}
