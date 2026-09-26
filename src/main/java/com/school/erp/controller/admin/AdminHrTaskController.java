package com.school.erp.controller.admin;

import com.school.erp.dto.hr.StaffTaskDTO;
import com.school.erp.dto.hr.StaffTaskRequest;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.StaffTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/admin/schools/{schoolId}/hr/tasks", "/api/v1/admin/hr/tasks"})
@RequiredArgsConstructor
public class AdminHrTaskController {

    private final StaffTaskService taskService;

    private Long resolveSchoolId(Long pathSchoolId, Long paramSchoolId) {
        Long schoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId must be provided either in the path or as a query parameter");
        }
        return schoolId;
    }

    @GetMapping
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<List<StaffTaskDTO>> getTasks(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        return ResponseEntity.ok(taskService.getTasks(resolveSchoolId(pathSchoolId, paramSchoolId), staffId, status, priority));
    }

    @GetMapping("/{id}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<StaffTaskDTO> getTaskById(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(resolveSchoolId(pathSchoolId, paramSchoolId), id));
    }

    @PostMapping
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<StaffTaskDTO> createTask(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @Valid @RequestBody StaffTaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(resolveSchoolId(pathSchoolId, paramSchoolId), request));
    }

    @PutMapping("/{id}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<StaffTaskDTO> updateTask(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @Valid @RequestBody StaffTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(resolveSchoolId(pathSchoolId, paramSchoolId), id, request));
    }

    @PatchMapping("/{id}/status")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<StaffTaskDTO> updateTaskStatus(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, String> body) {
        String effectiveStatus = status;
        if ((effectiveStatus == null || effectiveStatus.isEmpty()) && body != null && body.containsKey("status")) {
            effectiveStatus = body.get("status");
        }
        return ResponseEntity.ok(taskService.updateTaskStatus(resolveSchoolId(pathSchoolId, paramSchoolId), id, effectiveStatus));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Void> deleteTask(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        taskService.deleteTask(resolveSchoolId(pathSchoolId, paramSchoolId), id);
        return ResponseEntity.noContent().build();
    }
}
