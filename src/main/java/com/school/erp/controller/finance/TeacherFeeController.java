package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.payment.FeeReminderResponse;
import com.school.erp.dto.payment.SendFeeReminderRequest;
import com.school.erp.dto.payment.TeacherClassResponse;
import com.school.erp.service.TeacherFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import com.school.erp.security.PermissionRequired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/teacher")
@RequiredArgsConstructor
public class TeacherFeeController {

    private final TeacherFeeService teacherFeeService;

    @GetMapping("/classes")
    @PermissionRequired("fees.my_class_fees.view")
    public ResponseEntity<ApiResponse<List<TeacherClassResponse>>> getAssignedClasses(
            @RequestParam Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                teacherFeeService.getAssignedClasses(schoolId),
                "Assigned classes fetched successfully"
        ));
    }

    @PostMapping("/reminders")
    @PermissionRequired("fees.my_class_fees.send_reminder")
    public ResponseEntity<ApiResponse<Void>> sendReminders(
            @RequestParam Long schoolId,
            @Valid @RequestBody SendFeeReminderRequest request) {
        teacherFeeService.sendReminders(schoolId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Reminders sent successfully"));
    }

    @GetMapping("/reminders")
    @PermissionRequired("fees.reminder_history.view")
    public ResponseEntity<ApiResponse<Page<FeeReminderResponse>>> getReminderHistory(
            @RequestParam Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                teacherFeeService.getReminderHistory(schoolId, PageRequest.of(page, size)),
                "Reminder history fetched successfully"
        ));
    }
}
