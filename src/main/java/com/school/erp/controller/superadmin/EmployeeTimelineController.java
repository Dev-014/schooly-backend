package com.school.erp.controller.superadmin;

import com.school.erp.dto.EmployeeTimelineDTO;
import com.school.erp.service.superadmin.EmployeeTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/employee-timeline")
@RequiredArgsConstructor
public class EmployeeTimelineController {

    private final EmployeeTimelineService timelineService;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<EmployeeTimelineDTO>> getTimelineByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(timelineService.getTimelineByEmployeeId(employeeId));
    }
}
