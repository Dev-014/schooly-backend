package com.school.erp.controller.admin;

import com.school.erp.dto.hr.StaffCertificateDTO;
import com.school.erp.dto.hr.StaffCertificateRequest;
import com.school.erp.dto.hr.StaffIdCardDTO;
import com.school.erp.dto.hr.StaffIdCardGenerationRequest;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.hr.StaffCertificateAndIdCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/schools/{schoolId}/hr", "/api/v1/admin/hr"})
@RequiredArgsConstructor
public class AdminHrCertificateController {

    private final StaffCertificateAndIdCardService certificateService;

    private Long resolveSchoolId(Long pathSchoolId, Long paramSchoolId) {
        Long schoolId = pathSchoolId != null ? pathSchoolId : paramSchoolId;
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId must be provided either in the path or as a query parameter");
        }
        return schoolId;
    }

    @GetMapping("/certificates")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<List<StaffCertificateDTO>> getCertificates(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String certificateType,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(certificateService.getCertificates(resolveSchoolId(pathSchoolId, paramSchoolId), staffId, certificateType, status));
    }

    @PostMapping("/certificates/generate")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<StaffCertificateDTO> generateCertificate(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @Valid @RequestBody StaffCertificateRequest request) {
        return ResponseEntity.ok(certificateService.generateCertificate(resolveSchoolId(pathSchoolId, paramSchoolId), request));
    }

    @GetMapping("/id-cards")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<List<StaffIdCardDTO>> getIdCards(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId) {
        return ResponseEntity.ok(certificateService.getIdCards(resolveSchoolId(pathSchoolId, paramSchoolId)));
    }

    @PostMapping("/id-cards/generate")
    @PermissionRequired("staff_hr.staff_directory.view")
    public ResponseEntity<List<StaffIdCardDTO>> generateIdCards(
            @PathVariable(value = "schoolId", required = false) Long pathSchoolId,
            @RequestParam(value = "schoolId", required = false) Long paramSchoolId,
            @RequestBody(required = false) StaffIdCardGenerationRequest request) {
        return ResponseEntity.ok(certificateService.generateIdCards(resolveSchoolId(pathSchoolId, paramSchoolId), request));
    }
}
