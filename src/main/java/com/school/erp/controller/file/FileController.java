package com.school.erp.controller.file;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.file.*;
import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileEntityType;
import com.school.erp.enums.file.FileStatus;
import com.school.erp.security.PermissionRequired;
import com.school.erp.security.AuthContextService;
import com.school.erp.service.file.FileManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "Centralized File Management", description = "Direct-to-S3 uploads, secure downloads, validation, and lifecycle management")
public class FileController {

    private final FileManagementService fileManagementService;
    private final AuthContextService authContextService;

    @PostMapping("/upload-init")
    @PermissionRequired("files.file.upload")
    @Operation(summary = "Initiate file upload and obtain short-lived presigned S3 upload URL")
    public ResponseEntity<ApiResponse<FileUploadInitResponse>> initiateUpload(
            @Valid @RequestBody FileUploadInitRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        FileUploadInitResponse response = fileManagementService.initiateUpload(request, clientIp);
        return ResponseEntity.ok(ApiResponse.success(response, "Upload initiated successfully"));
    }

    @PostMapping("/{fileId}/complete")
    @PermissionRequired("files.file.upload")
    @Operation(summary = "Complete upload, verify S3 object, validate signatures, scan malware, and activate file")
    public ResponseEntity<ApiResponse<FileResponseDto>> completeUpload(
            @PathVariable UUID fileId,
            @RequestBody(required = false) FileUploadCompleteRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        FileResponseDto response = fileManagementService.completeUpload(fileId, request, clientIp);
        return ResponseEntity.ok(ApiResponse.success(response, "File verified and activated successfully"));
    }

    @GetMapping("/{fileId}")
    @PermissionRequired("files.file.view")
    @Operation(summary = "Get file metadata")
    public ResponseEntity<ApiResponse<FileResponseDto>> getFileMetadata(@PathVariable UUID fileId) {
        FileResponseDto response = fileManagementService.getFileMetadata(fileId);
        return ResponseEntity.ok(ApiResponse.success(response, "File metadata retrieved"));
    }

    @GetMapping("/{fileId}/download")
    @PermissionRequired({"files.file.download", "files.file.view"})
    @Operation(summary = "Generate short-lived presigned download or preview URL")
    public ResponseEntity<ApiResponse<FileDownloadResponse>> getDownloadUrl(
            @PathVariable UUID fileId,
            @RequestParam(defaultValue = "false") boolean preview,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        FileDownloadResponse response = fileManagementService.getDownloadUrl(fileId, preview, clientIp);
        return ResponseEntity.ok(ApiResponse.success(response, "Presigned download URL generated"));
    }

    @GetMapping
    @PermissionRequired("files.file.view")
    @Operation(summary = "List and filter files by entity, category, or status")
    public ResponseEntity<ApiResponse<List<FileResponseDto>>> listFiles(
            @RequestParam(required = false) FileEntityType entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) FileCategory category,
            @RequestParam(required = false) FileStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        FileFilterRequest filterRequest = new FileFilterRequest(entityType, entityId, category, status, page, size);
        Page<FileResponseDto> pageResult = fileManagementService.listFiles(filterRequest);

        PaginationMeta pagination = new PaginationMeta(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements()
        );

        return ResponseEntity.ok(ApiResponse.success(pageResult.getContent(), "Files retrieved successfully", pagination));
    }

    @PostMapping("/{fileId}/replace")
    @PermissionRequired("files.file.replace")
    @Operation(summary = "Replace an existing file with a new version")
    public ResponseEntity<ApiResponse<FileUploadInitResponse>> replaceFile(
            @PathVariable UUID fileId,
            @Valid @RequestBody FileUploadInitRequest newFileRequest,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        FileUploadInitResponse response = fileManagementService.replaceFile(fileId, newFileRequest, clientIp);
        return ResponseEntity.ok(ApiResponse.success(response, "Replacement upload initiated"));
    }

    @DeleteMapping("/{fileId}")
    @PermissionRequired("files.file.delete")
    @Operation(summary = "Soft delete file and unlink attachment")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable UUID fileId,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        fileManagementService.deleteFile(fileId, clientIp);
        return ResponseEntity.ok(ApiResponse.success(null, "File deleted successfully"));
    }

    @PostMapping("/initialize-folders")
    @PermissionRequired("files.file.upload")
    @Operation(summary = "Initialize standard use-case folders for a school in S3")
    public ResponseEntity<ApiResponse<String>> initializeSchoolFolders(
            @RequestParam(required = false) Long schoolId
    ) {
        Long targetSchoolId = schoolId != null ? schoolId : authContextService.requireCurrentUser().schoolId();
        if (targetSchoolId == null) {
            throw new IllegalArgumentException("schoolId is required to initialize bucket folders");
        }
        fileManagementService.initializeSchoolBucketFolders(targetSchoolId);
        return ResponseEntity.ok(ApiResponse.success("Standard use-case folders initialized successfully", "Folders created"));
    }

    @RequestMapping(value = "/mock-s3-upload/**", method = {RequestMethod.PUT, RequestMethod.POST})
    @Operation(summary = "Mock S3 upload endpoint for local development without real AWS credentials")
    public ResponseEntity<Void> mockS3Upload() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/mock-s3-download/**")
    @Operation(summary = "Mock S3 download endpoint for local development without real AWS credentials")
    public ResponseEntity<byte[]> mockS3Download(
            @RequestParam(defaultValue = "inline") String disposition,
            @RequestParam(defaultValue = "document.pdf") String file
    ) {
        byte[] samplePdf = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R >>\nendobj\n4 0 obj\n<< /Length 44 >>\nstream\nBT /F1 24 Tf 100 700 Td (Schooly Document Preview) ET\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n0000000115 00000 n \n0000000214 00000 n \ntrailer\n<< /Size 5 /Root 1 0 R >>\nstartxref\n309\n%%EOF".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                "inline".equalsIgnoreCase(disposition)
                        ? org.springframework.http.ContentDisposition.inline().filename(file).build()
                        : org.springframework.http.ContentDisposition.attachment().filename(file).build()
        );
        return new ResponseEntity<>(samplePdf, headers, org.springframework.http.HttpStatus.OK);
    }
}
