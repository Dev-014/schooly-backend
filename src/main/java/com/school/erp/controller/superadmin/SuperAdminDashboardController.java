package com.school.erp.controller.superadmin;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.superadmin.*;
import com.school.erp.service.superadmin.SuperAdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/super-admin/dashboard", "/api/v1/super-admin/dashboard"})
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService dashboardService;

    public SuperAdminDashboardController(SuperAdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<SuperAdminDashboardMetricsDto>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getMetrics(), "Dashboard executive metrics fetched successfully"));
    }

    @GetMapping("/revenue-growth")
    public ResponseEntity<ApiResponse<List<RevenueGrowthPointDto>>> getRevenueGrowth() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getRevenueGrowth(), "Revenue growth metrics fetched successfully"));
    }

    @GetMapping("/pipeline")
    public ResponseEntity<ApiResponse<List<PipelineSchoolDto>>> getPipeline() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getPipelineSchools(), "Onboarding pipeline fetched successfully"));
    }

    @GetMapping("/module-adoption")
    public ResponseEntity<ApiResponse<List<ModuleAdoptionDto>>> getModuleAdoption() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getModuleAdoption(), "Module adoption rates fetched successfully"));
    }

    @GetMapping("/operational-alerts")
    public ResponseEntity<ApiResponse<List<OperationalAlertDto>>> getOperationalAlerts() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getOperationalAlerts(), "Operational alerts fetched successfully"));
    }

    @GetMapping("/sales-funnel")
    public ResponseEntity<ApiResponse<List<SalesFunnelStepDto>>> getSalesFunnel() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSalesFunnel(), "Sales funnel fetched successfully"));
    }

    @GetMapping("/retention-curve")
    public ResponseEntity<ApiResponse<List<RetentionPointDto>>> getRetentionCurve() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getRetentionCurve(), "Retention curve fetched successfully"));
    }

    @GetMapping("/support-metrics")
    public ResponseEntity<ApiResponse<SupportMetricsDto>> getSupportMetrics() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSupportMetrics(), "Support metrics fetched successfully"));
    }

    @GetMapping("/upcoming-renewals")
    public ResponseEntity<ApiResponse<List<UpcomingRenewalDto>>> getUpcomingRenewals() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getUpcomingRenewals(), "Upcoming renewals fetched successfully"));
    }

    @GetMapping("/onboarding-matrix")
    public ResponseEntity<ApiResponse<List<OnboardingMatrixDto>>> getOnboardingMatrix() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getOnboardingMatrix(), "Onboarding matrix fetched successfully"));
    }

    @GetMapping("/platform-health")
    public ResponseEntity<ApiResponse<PlatformHealthDto>> getPlatformHealth() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getPlatformHealth(), "Platform health fetched successfully"));
    }
}

