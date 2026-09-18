package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.dashboard.ActivityFeedResponse;
import com.school.erp.dto.dashboard.CollectionExpensePointResponse;
import com.school.erp.dto.dashboard.DashboardKpiResponse;
import com.school.erp.service.DashboardService;
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
