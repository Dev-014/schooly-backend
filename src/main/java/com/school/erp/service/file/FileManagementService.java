package com.school.erp.service.file;

import com.school.erp.dto.file.*;
import com.school.erp.entity.auth.User;
import com.school.erp.entity.file.FileAttachment;
import com.school.erp.entity.file.FileMetadata;
import com.school.erp.entity.superadmin.School;
import com.school.erp.enums.file.FileAuditAction;
import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileStatus;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ForbiddenException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.auth.UserRepository;
import com.school.erp.repository.file.FileAttachmentRepository;
import com.school.erp.repository.file.FileMetadataRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.storage.ObjectStorageProvider;
import com.school.erp.storage.PresignedUploadResponse;
import com.school.erp.storage.StorageObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagementService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final ObjectStorageProvider storageProvider;
    private final FileValidationService fileValidationService;
    private final FileSignatureValidator fileSignatureValidator;
    private final ClamAvScannerService clamAvScannerService;
    private final FileSecurityService fileSecurityService;
    private final FileAuditService fileAuditService;
    private final AuthContextService authContextService;
    private final FileStoragePathService fileStoragePathService;

    @Value("${app.aws.s3.bucket:${AWS_S3_BUCKET:schooly-dev-files}}")
    private String defaultBucketName;

    @Value("${app.file-management.download-url-expiration-minutes:15}")
    private int downloadExpirationMinutes;

    @Transactional
    public FileUploadInitResponse initiateUpload(FileUploadInitRequest request, String ipAddress) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Long schoolId = authContextService.resolveSchoolId(currentUser.schoolId());

        fileValidationService.validateUploadRequest(schoolId, request);
        fileSecurityService.validateEntityScope(request.entityType(), request.entityId(), currentUser, schoolId);

        String extension = fileValidationService.extractExtension(request.fileName());
        UUID fileId = UUID.randomUUID();

        // Strictly segregate S3 folders per use-case
        String folderPath = fileStoragePathService.resolveFolderPath(schoolId, request.entityType(), request.entityId(), request.category());
        storageProvider.createFolder(defaultBucketName, folderPath);

        String storageKey = fileStoragePathService.generateObjectKey(
                schoolId, request.entityType(), request.entityId(), request.category(), fileId, extension);

        Duration presignDuration = Duration.ofMinutes(15);
        PresignedUploadResponse presignedUpload = storageProvider.generatePresignedUploadUrl(
                defaultBucketName, storageKey, request.contentType(), request.fileSize(), presignDuration);

        School school = schoolRepository.getReferenceById(schoolId);
        User user = currentUser.userId() != null ? userRepository.findById(currentUser.userId()).orElse(null) : null;

        FileMetadata fileMetadata = FileMetadata.builder()
                .id(fileId)
                .school(school)
                .originalFileName(request.fileName())
                .storageProvider("S3")
                .bucketName(defaultBucketName)
                .storageKey(storageKey)
                .mimeType(request.contentType())
                .fileExtension(extension)
                .fileSizeBytes(request.fileSize())
                .status(FileStatus.PENDING_UPLOAD)
                .uploadedBy(user)
                .build();

        fileMetadataRepository.save(fileMetadata);

        // Pre-create the attachment record in pending state
        FileAttachment attachment = FileAttachment.builder()
                .school(school)
                .file(fileMetadata)
                .entityType(request.entityType())
                .entityId(request.entityId())
                .category(request.category())
                .createdBy(user)
                .build();
        fileAttachmentRepository.save(attachment);

        fileAuditService.recordEvent(school, fileMetadata, user, FileAuditAction.UPLOAD_INITIATED, ipAddress,
                String.format("{\"fileName\":\"%s\",\"size\":%d,\"category\":\"%s\"}",
                        request.fileName(), request.fileSize(), request.category()));

        return new FileUploadInitResponse(
                fileId,
                presignedUpload.uploadUrl(),
                presignedUpload.httpMethod(),
                storageKey,
                presignedUpload.requiredHeaders(),
                presignedUpload.expiresAt()
        );
    }

    @Transactional
    public FileResponseDto completeUpload(UUID fileId, FileUploadCompleteRequest completeRequest, String ipAddress) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        FileMetadata file = fileMetadataRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File record not found for id: " + fileId));

        fileSecurityService.validateAccess(file, currentUser);

        // Idempotency: if already active, return the existing active DTO
        if (file.getStatus() == FileStatus.ACTIVE) {
            log.info("File {} is already ACTIVE; returning idempotent completion result", fileId);
            return toResponseDto(file);
        }

        // Verify object existence in S3
        boolean exists = storageProvider.doesObjectExist(file.getBucketName(), file.getStorageKey());
        if (!exists) {
            throw new BadRequestException("Object does not exist in storage. Please complete the direct upload first.");
        }

        StorageObjectMetadata metadata = storageProvider.getObjectMetadata(file.getBucketName(), file.getStorageKey());
        file.setFileSizeBytes(metadata.sizeBytes());

        // Validate File Signature & Scan for Malware
        try (InputStream inputStream = storageProvider.getObjectInputStream(file.getBucketName(), file.getStorageKey())) {
            fileSignatureValidator.validateFileSignature(inputStream, file.getMimeType(), file.getFileExtension());
        } catch (BadRequestException e) {
            file.setStatus(FileStatus.FAILED);
            fileMetadataRepository.save(file);
            throw e;
        } catch (Exception e) {
            log.error("Failed to read object stream for signature verification: {}", fileId, e);
            throw new BadRequestException("Verification failed: " + e.getMessage());
        }

        try (InputStream inputStream = storageProvider.getObjectInputStream(file.getBucketName(), file.getStorageKey())) {
            clamAvScannerService.scanInputStream(inputStream, file.getOriginalFileName());
        } catch (BadRequestException e) {
            file.setStatus(FileStatus.REJECTED);
            fileMetadataRepository.save(file);
            storageProvider.deleteObject(file.getBucketName(), file.getStorageKey());

            User user = currentUser.userId() != null ? userRepository.findById(currentUser.userId()).orElse(null) : null;
            fileAuditService.recordEvent(file.getSchool(), file, user, FileAuditAction.FILE_REJECTED, ipAddress,
                    "{\"reason\":\"Malware scan failed: " + e.getMessage() + "\"}");
            throw e;
        } catch (Exception e) {
            log.warn("ClamAV scan skipped or encountered non-fatal error: {}", e.getMessage());
        }

        if (completeRequest != null && completeRequest.checksumSha256() != null) {
            file.setChecksumSha256(completeRequest.checksumSha256());
        }

        file.setStatus(FileStatus.ACTIVE);
        fileMetadataRepository.save(file);

        User user = currentUser.userId() != null ? userRepository.findById(currentUser.userId()).orElse(null) : null;
        fileAuditService.recordEvent(file.getSchool(), file, user, FileAuditAction.UPLOAD_COMPLETED, ipAddress,
                String.format("{\"size\":%d,\"status\":\"ACTIVE\"}", file.getFileSizeBytes()));

        // Auto-supersede and soft-delete any older active files for the same entity and category
        fileAttachmentRepository.findByFileId(file.getId()).ifPresent(currentAtt -> {
            if (currentAtt.getEntityType() != null && currentAtt.getEntityId() != null 
                    && currentAtt.getCategory() != null && currentAtt.getCategory() != FileCategory.OTHER) {
                List<FileAttachment> existingAttachments = fileAttachmentRepository
                        .findBySchoolIdAndEntityTypeAndEntityIdAndCategoryOrderByDisplayOrderAsc(
                                file.getSchool().getId(), currentAtt.getEntityType(), currentAtt.getEntityId(), currentAtt.getCategory());
                for (FileAttachment oldAtt : existingAttachments) {
                    if (!oldAtt.getFile().getId().equals(file.getId())) {
                        FileMetadata oldFile = oldAtt.getFile();
                        if (oldFile.getDeletedAt() == null && oldFile.getStatus() == FileStatus.ACTIVE) {
                            oldFile.setDeletedAt(LocalDateTime.now());
                            oldFile.setStatus(FileStatus.DELETED);
                            fileMetadataRepository.save(oldFile);
                            fileAuditService.recordEvent(file.getSchool(), oldFile, user, FileAuditAction.FILE_REPLACED, ipAddress,
                                    String.format("{\"supersededBy\":\"%s\",\"category\":\"%s\"}", file.getId(), currentAtt.getCategory()));
                        }
                    }
                }
            }
        });

        return toResponseDto(file);
    }

    @Transactional(readOnly = true)
    public FileResponseDto getFileMetadata(UUID fileId) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        FileMetadata file = fileMetadataRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found for id: " + fileId));

        fileSecurityService.validateAccess(file, currentUser);
        return toResponseDto(file);
    }

    @Transactional
    public FileDownloadResponse getDownloadUrl(UUID fileId, boolean isInline, String ipAddress) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        FileMetadata file = fileMetadataRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found for id: " + fileId));

        fileSecurityService.validateAccess(file, currentUser);

        if (file.getStatus() != FileStatus.ACTIVE) {
            throw new ForbiddenException("File cannot be accessed because its status is: " + file.getStatus());
        }

        Duration duration = Duration.ofMinutes(downloadExpirationMinutes);
        String downloadUrl = storageProvider.generatePresignedDownloadUrl(
                file.getBucketName(), file.getStorageKey(), file.getOriginalFileName(), isInline, duration);

        User user = currentUser.userId() != null ? userRepository.findById(currentUser.userId()).orElse(null) : null;
        FileAuditAction action = isInline ? FileAuditAction.FILE_VIEWED : FileAuditAction.FILE_DOWNLOADED;
        fileAuditService.recordEvent(file.getSchool(), file, user, action, ipAddress,
                String.format("{\"isInline\":%b,\"fileName\":\"%s\"}", isInline, file.getOriginalFileName()));

        return new FileDownloadResponse(
                file.getId(),
                downloadUrl,
                file.getOriginalFileName(),
                file.getMimeType(),
                file.getFileSizeBytes(),
                isInline,
                java.time.Instant.now().plus(duration)
        );
    }

    @Transactional
    public FileUploadInitResponse replaceFile(UUID oldFileId, FileUploadInitRequest newFileRequest, String ipAddress) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        FileMetadata oldFile = fileMetadataRepository.findByIdAndDeletedAtIsNull(oldFileId)
                .orElseThrow(() -> new ResourceNotFoundException("File to replace not found: " + oldFileId));

        fileSecurityService.validateAccess(oldFile, currentUser);

        // Soft delete old file
        oldFile.setDeletedAt(LocalDateTime.now());
        oldFile.setStatus(FileStatus.DELETED);
        fileMetadataRepository.save(oldFile);

        // Remove active attachment of old file
        fileAttachmentRepository.deleteByFileId(oldFileId);

        User user = currentUser.userId() != null ? userRepository.findById(currentUser.userId()).orElse(null) : null;
        fileAuditService.recordEvent(oldFile.getSchool(), oldFile, user, FileAuditAction.FILE_REPLACED, ipAddress,
                String.format("{\"replacedBy\":\"%s\"}", newFileRequest.fileName()));

        // Initiate new upload with same entity and category
        return initiateUpload(newFileRequest, ipAddress);
    }

    @Transactional
    public void deleteFile(UUID fileId, String ipAddress) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        FileMetadata file = fileMetadataRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found for id: " + fileId));

        fileSecurityService.validateAccess(file, currentUser);

        file.setDeletedAt(LocalDateTime.now());
        file.setStatus(FileStatus.DELETED);
        fileMetadataRepository.save(file);

        fileAttachmentRepository.deleteByFileId(fileId);

        User user = currentUser.userId() != null ? userRepository.findById(currentUser.userId()).orElse(null) : null;
        fileAuditService.recordEvent(file.getSchool(), file, user, FileAuditAction.FILE_DELETED, ipAddress, "{}");
    }

    @Transactional(readOnly = true)
    public Page<FileResponseDto> listFiles(FileFilterRequest request) {
        AuthenticatedUser currentUser = authContextService.requireCurrentUser();
        Long schoolId = authContextService.resolveSchoolId(currentUser.schoolId());

        if (request.entityType() != null && request.entityId() != null) {
            fileSecurityService.validateEntityScope(request.entityType(), request.entityId(), currentUser, schoolId);
            List<FileAttachment> attachments = request.category() != null
                    ? fileAttachmentRepository.findBySchoolIdAndEntityTypeAndEntityIdAndCategoryOrderByDisplayOrderAsc(
                            schoolId, request.entityType(), request.entityId(), request.category())
                    : fileAttachmentRepository.findBySchoolIdAndEntityTypeAndEntityIdOrderByDisplayOrderAscCreatedAtDesc(
                            schoolId, request.entityType(), request.entityId());

            FileStatus targetStatus = request.status() != null ? request.status() : FileStatus.ACTIVE;
            List<FileResponseDto> dtos = attachments.stream()
                    .filter(att -> att.getFile().getDeletedAt() == null && att.getFile().getStatus() == targetStatus)
                    .map(att -> toResponseDto(att.getFile(), att))
                    .toList();

            // Retain only the latest document per category slot (excluding OTHER)
            Map<FileCategory, FileResponseDto> latestByCategory = new LinkedHashMap<>();
            List<FileResponseDto> deduplicatedDtos = new ArrayList<>();
            for (FileResponseDto dto : dtos) {
                if (dto.category() != null && dto.category() != FileCategory.OTHER) {
                    if (!latestByCategory.containsKey(dto.category())) {
                        latestByCategory.put(dto.category(), dto);
                        deduplicatedDtos.add(dto);
                    }
                } else {
                    deduplicatedDtos.add(dto);
                }
            }

            int page = request.page() != null && request.page() >= 0 ? request.page() : 0;
            int size = request.size() != null && request.size() > 0 ? request.size() : 20;
            int start = Math.min(page * size, deduplicatedDtos.size());
            int end = Math.min(start + size, deduplicatedDtos.size());

            return new org.springframework.data.domain.PageImpl<>(
                    deduplicatedDtos.subList(start, end),
                    PageRequest.of(page, size),
                    deduplicatedDtos.size()
            );
        }

        Pageable pageable = PageRequest.of(
                request.page() != null && request.page() >= 0 ? request.page() : 0,
                request.size() != null && request.size() > 0 ? request.size() : 20,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return fileMetadataRepository.searchFiles(schoolId, request.status(), pageable)
                .map(this::toResponseDto);
    }

    /**
     * Pre-provisions all standard use-case folders for a school in Amazon S3.
     */
    public void initializeSchoolBucketFolders(Long schoolId) {
        List<String> folders = fileStoragePathService.getStandardSchoolFolders(schoolId);
        for (String folder : folders) {
            storageProvider.createFolder(defaultBucketName, folder);
        }
        log.info("Initialized {} use-case folders in S3 bucket '{}' for schoolId={}", folders.size(), defaultBucketName, schoolId);
    }

    private FileResponseDto toResponseDto(FileMetadata file) {
        FileAttachment attachment = fileAttachmentRepository.findByFileId(file.getId()).orElse(null);
        return toResponseDto(file, attachment);
    }

    private FileResponseDto toResponseDto(FileMetadata file, FileAttachment attachment) {
        return new FileResponseDto(
                file.getId(),
                file.getSchool().getId(),
                file.getOriginalFileName(),
                file.getMimeType(),
                file.getFileExtension(),
                file.getFileSizeBytes(),
                file.getStatus(),
                file.getStorageKey(),
                attachment != null ? attachment.getCategory() : null,
                attachment != null ? attachment.getEntityType() : null,
                attachment != null ? attachment.getEntityId() : null,
                attachment != null ? attachment.getDisplayOrder() : 0,
                file.getUploadedBy() != null ? file.getUploadedBy().getId() : null,
                file.getCreatedAt()
        );
    }
}

