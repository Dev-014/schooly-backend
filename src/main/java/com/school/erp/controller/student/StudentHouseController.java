package com.school.erp.controller.student;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.student.StudentHouseRequest;
import com.school.erp.dto.student.StudentHouseResponse;
import com.school.erp.dto.student.StudentResponse;
import com.school.erp.service.StudentHouseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-houses")
public class StudentHouseController {

    private final StudentHouseService houseService;

    public StudentHouseController(StudentHouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentHouseResponse>>> getAllHouses(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(houseService.getAllHouses(schoolId), "Student houses retrieved successfully"));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getStudentsByHouse(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(houseService.getStudentsByHouse(id, schoolId), "Students retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentHouseResponse>> createHouse(
            @Valid @RequestBody StudentHouseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(houseService.createHouse(request), "Student house created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentHouseResponse>> updateHouse(
            @PathVariable Long id,
            @Valid @RequestBody StudentHouseRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(houseService.updateHouse(id, request), "Student house updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHouse(
            @PathVariable Long id,
            @RequestParam(required = false) Long schoolId
    ) {
        houseService.deleteHouse(id, schoolId);
        return ResponseEntity.ok(ApiResponse.success(null, "Student house deleted successfully"));
    }
}

