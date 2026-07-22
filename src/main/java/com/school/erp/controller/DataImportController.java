package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.importing.DataImportErrorDTO;
import com.school.erp.dto.importing.DataImportJobDTO;
import com.school.erp.dto.importing.ResolveErrorRequest;
import com.school.erp.service.DataImportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/import", "/api/v1/import", "/api/v1/onboarding/import"})
public class DataImportController {

    private final DataImportService dataImportService;

    public DataImportController(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DataImportJobDTO>> uploadFile(@RequestParam("schoolId") Long schoolId,
                                                                    @RequestParam("category") String category,
                                                                    @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                dataImportService.uploadFile(schoolId, category, file),
                "File uploaded and parsed with AI column intelligence successfully"
        ));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<DataImportJobDTO>> getJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                dataImportService.getJob(jobId),
                "Import job retrieved successfully"
        ));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<DataImportJobDTO>>> listJobs(@RequestParam("schoolId") Long schoolId,
                                                                        @RequestParam(value = "category", required = false) String category) {
        return ResponseEntity.ok(ApiResponse.success(
                dataImportService.listJobs(schoolId, category),
                "Import jobs listed successfully"
        ));
    }

    @PostMapping("/errors/{errorId}/resolve")
    public ResponseEntity<ApiResponse<DataImportErrorDTO>> resolveError(@PathVariable Long errorId,
                                                                        @Valid @RequestBody ResolveErrorRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                dataImportService.resolveError(errorId, request.newValue()),
                "Validation error resolved inline successfully"
        ));
    }

    @PostMapping("/job/{jobId}/commit")
    public ResponseEntity<ApiResponse<DataImportJobDTO>> commitJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                dataImportService.commitJob(jobId),
                "Data import job committed and affected modules updated successfully"
        ));
    }
}
