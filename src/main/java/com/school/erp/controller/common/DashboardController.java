package com.school.erp.controller.common;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.dashboard.ActivityFeedResponse;
import com.school.erp.dto.dashboard.CollectionExpensePointResponse;
import com.school.erp.dto.dashboard.DashboardBreakdownResponse;
import com.school.erp.dto.dashboard.DashboardKpiResponse;
import com.school.erp.service.common.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<ApiResponse<DashboardKpiResponse>> getKpis(@RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getKpis(schoolId),
                "Dashboard KPIs fetched successfully"
        ));
    }

    @GetMapping("/chart/collection-vs-expenses")
    public ResponseEntity<ApiResponse<List<CollectionExpensePointResponse>>> getCollectionVsExpenses(
            @RequestParam(required = false) Long schoolId,
            @RequestParam int year
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getCollectionVsExpense(schoolId, year),
                "Collection vs expenses chart fetched successfully"
        ));
    }

    @GetMapping("/earning-modes")
    public ResponseEntity<ApiResponse<List<DashboardBreakdownResponse.EarningModeItem>>> getEarningModes(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getEarningModes(schoolId),
                "Dashboard earning modes fetched successfully"
        ));
    }

    @GetMapping("/fee-distribution")
    public ResponseEntity<ApiResponse<List<DashboardBreakdownResponse.FeeDistributionItem>>> getFeeDistribution(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getFeeDistribution(schoolId),
                "Dashboard fee distribution fetched successfully"
        ));
    }

    @GetMapping("/class-distribution")
    public ResponseEntity<ApiResponse<List<DashboardBreakdownResponse.ClassDistributionItem>>> getClassDistribution(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getClassDistribution(schoolId),
                "Dashboard class distribution fetched successfully"
        ));
    }

    @GetMapping("/student-birthdays")
    public ResponseEntity<ApiResponse<List<DashboardBreakdownResponse.StudentBirthdayItem>>> getStudentBirthdays(
            @RequestParam(required = false) Long schoolId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getStudentBirthdays(schoolId),
                "Dashboard student birthdays fetched successfully"
        ));
    }

    @GetMapping("/activity-feed")
    public ResponseEntity<ApiResponse<List<ActivityFeedResponse>>> getActivityFeed(
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                dashboardService.getActivityFeed(size),
                "Activity feed fetched successfully"
        ));
    }
}
