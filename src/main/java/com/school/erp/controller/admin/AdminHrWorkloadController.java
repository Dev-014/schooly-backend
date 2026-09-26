package com.school.erp.controller.admin;

import com.school.erp.dto.hr.TeacherWorkloadAnalyticsDTO;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.TeacherWorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/admin/schools/{schoolId}/hr/workload", "/api/v1/admin/hr/workload"})
@RequiredArgsConstructor
public class AdminHrWorkloadController {

    private final TeacherWorkloadService workloadService;

    private Long resolveSchoolId(Long pathSchoolId, Long paramSchoolId) {
        Long schoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId must be provided either in the path or as a query parameter");
        }
        return schoolId;
    }

    @GetMapping("/analytics")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<TeacherWorkloadAnalyticsDTO> getWorkloadAnalytics(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(workloadService.getWorkloadAnalytics(
                resolveSchoolId(pathSchoolId, paramSchoolId), academicYearId, departmentId));
    }
}
