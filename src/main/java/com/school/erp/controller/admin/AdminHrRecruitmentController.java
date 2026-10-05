package com.school.erp.controller.admin;

import com.school.erp.dto.hr.RecruitmentCandidateDTO;
import com.school.erp.dto.hr.RecruitmentCandidateRequest;
import com.school.erp.entity.hr.Staff;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.RecruitmentCandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/admin/schools/{schoolId}/hr/recruitment/candidates", "/api/v1/admin/hr/recruitment/candidates"})
@RequiredArgsConstructor
public class AdminHrRecruitmentController {

    private final RecruitmentCandidateService candidateService;

    private Long resolveSchoolId(Long pathSchoolId, Long paramSchoolId) {
        Long schoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId must be provided either in the path or as a query parameter");
        }
        return schoolId;
    }

    @GetMapping
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<List<RecruitmentCandidateDTO>> getCandidates(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(candidateService.getCandidates(resolveSchoolId(pathSchoolId, paramSchoolId), role, status, search));
    }

    @GetMapping("/{id}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<RecruitmentCandidateDTO> getCandidateById(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(resolveSchoolId(pathSchoolId, paramSchoolId), id));
    }

    @PostMapping
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<RecruitmentCandidateDTO> createCandidate(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @Valid @RequestBody RecruitmentCandidateRequest request) {
        return ResponseEntity.ok(candidateService.createCandidate(resolveSchoolId(pathSchoolId, paramSchoolId), request));
    }

    @PutMapping("/{id}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<RecruitmentCandidateDTO> updateCandidate(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @Valid @RequestBody RecruitmentCandidateRequest request) {
        return ResponseEntity.ok(candidateService.updateCandidate(resolveSchoolId(pathSchoolId, paramSchoolId), id, request));
    }

    @PatchMapping("/{id}/status")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<RecruitmentCandidateDTO> updateCandidateStatus(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, String> body) {
        String effectiveStatus = status;
        if ((effectiveStatus == null || effectiveStatus.isEmpty()) && body != null && body.containsKey("status")) {
            effectiveStatus = body.get("status");
        }
        return ResponseEntity.ok(candidateService.updateCandidateStatus(resolveSchoolId(pathSchoolId, paramSchoolId), id, effectiveStatus));
    }

    @PostMapping("/{id}/convert-to-staff")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Staff> convertCandidateToStaff(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(candidateService.convertCandidateToStaff(resolveSchoolId(pathSchoolId, paramSchoolId), id));
    }

    @DeleteMapping("/{id}")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<Void> deleteCandidate(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @PathVariable Long id) {
        candidateService.deleteCandidate(resolveSchoolId(pathSchoolId, paramSchoolId), id);
        return ResponseEntity.noContent().build();
    }
}
