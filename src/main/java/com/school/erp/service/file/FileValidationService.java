package com.school.erp.service.file;

import com.school.erp.dto.file.FileUploadInitRequest;
import com.school.erp.enums.file.FileCategory;
import com.school.erp.exception.BadRequestException;
import com.school.erp.repository.file.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileValidationService {

    private final FileMetadataRepository fileMetadataRepository;

    @Value("${app.file-management.school-quota-bytes:10737418240}") // Default 10GB per school
    private long defaultSchoolQuotaBytes;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "csv"
    );

    private static final Map<FileCategory, Long> CATEGORY_SIZE_LIMITS = Map.ofEntries(
            Map.entry(FileCategory.STUDENT_PHOTO, 2L * 1024 * 1024),                // 2 MB
            Map.entry(FileCategory.BIRTH_CERTIFICATE, 10L * 1024 * 1024),          // 10 MB
            Map.entry(FileCategory.TRANSFER_CERTIFICATE, 10L * 1024 * 1024),       // 10 MB
            Map.entry(FileCategory.PREVIOUS_MARKSHEET, 10L * 1024 * 1024),         // 10 MB
            Map.entry(FileCategory.IDENTITY_PROOF, 10L * 1024 * 1024),             // 10 MB
            Map.entry(FileCategory.ADDRESS_PROOF, 10L * 1024 * 1024),              // 10 MB
            Map.entry(FileCategory.MEDICAL_CERTIFICATE, 10L * 1024 * 1024),        // 10 MB
            Map.entry(FileCategory.REPORT_CARD, 15L * 1024 * 1024),                // 15 MB
            Map.entry(FileCategory.HOMEWORK_ATTACHMENT, 25L * 1024 * 1024),        // 25 MB
            Map.entry(FileCategory.SUBMISSION_ATTACHMENT, 25L * 1024 * 1024),      // 25 MB
            Map.entry(FileCategory.STAFF_ID, 5L * 1024 * 1024),                    // 5 MB
            Map.entry(FileCategory.STAFF_RESUME, 10L * 1024 * 1024),               // 10 MB
            Map.entry(FileCategory.STAFF_CERTIFICATE, 10L * 1024 * 1024),          // 10 MB
            Map.entry(FileCategory.OTHER, 20L * 1024 * 1024)                       // 20 MB
    );

    public String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BadRequestException("File name must include a valid extension");
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase().trim();
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file extension: ." + extension);
        }
        return extension;
    }

    public void validateUploadRequest(Long schoolId, FileUploadInitRequest request) {
        String extension = extractExtension(request.fileName());

        // Validate Category Max Size
        long maxAllowedBytes = CATEGORY_SIZE_LIMITS.getOrDefault(request.category(), 20L * 1024 * 1024);
        if (request.fileSize() > maxAllowedBytes) {
            throw new BadRequestException(String.format(
                    "File size %d bytes exceeds the maximum allowed size of %d bytes for category %s",
                    request.fileSize(), maxAllowedBytes, request.category()
            ));
        }

        // Validate School Storage Quota
        long currentUsedBytes = fileMetadataRepository.calculateTotalUsedBytesBySchoolId(schoolId);
        if (currentUsedBytes + request.fileSize() > defaultSchoolQuotaBytes) {
            throw new BadRequestException("School storage quota exceeded. Please contact platform administrator.");
        }
    }
}
