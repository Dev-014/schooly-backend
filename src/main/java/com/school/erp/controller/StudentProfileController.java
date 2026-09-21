package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.StudentProfileResponse;
import com.school.erp.dto.student.UpdateStudentProfileRequest;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.StudentProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/student/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getProfile(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long schoolId
    ) {
        AuthenticatedUser authUser = AuthContextHolder.get();
        Long effectiveUserId = authUser != null ? authUser.userId() : null;
        Long effectiveSchoolId = schoolId != null ? schoolId : (authUser != null ? authUser.schoolId() : 1L);

        StudentProfileResponse response = studentProfileService.getProfile(studentId, effectiveSchoolId, effectiveUserId);
        return ResponseEntity.ok(ApiResponse.success(response, "Student profile fetched successfully"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<StudentProfileResponse>> updateProfile(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody UpdateStudentProfileRequest request
    ) {
        AuthenticatedUser authUser = AuthContextHolder.get();
        Long effectiveUserId = authUser != null ? authUser.userId() : null;
        Long effectiveSchoolId = schoolId != null ? schoolId : (authUser != null ? authUser.schoolId() : 1L);

        StudentProfileResponse response = studentProfileService.updateProfile(studentId, effectiveSchoolId, effectiveUserId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Student profile updated successfully"));
    }
}
