package com.school.erp.service.file;

import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileEntityType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStoragePathService {

    /**
     * Resolves the strict use-case folder directory prefix for S3 storage.
     * Always ends with a trailing slash ('/').
     */
    public String resolveFolderPath(Long schoolId, FileEntityType entityType, Long entityId, FileCategory category) {
        Long safeEntityId = entityId != null ? entityId : 0L;
        String categorySubfolder = resolveCategorySubfolder(category);

        return switch (entityType) {
            case STUDENT -> String.format("schools/%d/students/%d/%s/", schoolId, safeEntityId, categorySubfolder);
            case HOMEWORK_ASSIGNMENT -> String.format("schools/%d/homework/assignments/%d/", schoolId, safeEntityId);
            case HOMEWORK_SUBMISSION -> String.format("schools/%d/homework/submissions/%d/", schoolId, safeEntityId);
            case EXAM_REPORT_CARD -> String.format("schools/%d/examinations/report-cards/%d/", schoolId, safeEntityId);
            case STAFF -> String.format("schools/%d/staff/%d/%s/", schoolId, safeEntityId, categorySubfolder);
            case SCHOOL -> String.format("schools/%d/school/branding/", schoolId);
            case GENERAL -> String.format("schools/%d/general/%s/", schoolId, categorySubfolder);
        };
    }

    /**
     * Generates the complete, deterministic S3 object key inside the strict use-case folder.
     */
    public String generateObjectKey(Long schoolId, FileEntityType entityType, Long entityId, FileCategory category, UUID fileId, String extension) {
        String folderPath = resolveFolderPath(schoolId, entityType, entityId, category);
        String cleanExtension = (extension != null && !extension.isBlank())
                ? extension.toLowerCase(Locale.ROOT).replaceFirst("^\\.", "")
                : "bin";
        return folderPath + fileId + "." + cleanExtension;
    }

    /**
     * Returns the list of standard top-level and use-case folders to provision for a school.
     */
    public List<String> getStandardSchoolFolders(Long schoolId) {
        return List.of(
                String.format("schools/%d/students/", schoolId),
                String.format("schools/%d/examinations/report-cards/", schoolId),
                String.format("schools/%d/homework/assignments/", schoolId),
                String.format("schools/%d/homework/submissions/", schoolId),
                String.format("schools/%d/staff/", schoolId),
                String.format("schools/%d/finance/invoices/", schoolId),
                String.format("schools/%d/finance/receipts/", schoolId),
                String.format("schools/%d/school/branding/", schoolId),
                String.format("schools/%d/general/", schoolId)
        );
    }

    private String resolveCategorySubfolder(FileCategory category) {
        if (category == null) {
            return "documents";
        }
        return switch (category) {
            case STUDENT_PHOTO -> "photos";
            case BIRTH_CERTIFICATE -> "birth-certificates";
            case TRANSFER_CERTIFICATE -> "transfer-certificates";
            case PREVIOUS_MARKSHEET -> "previous-marksheets";
            case IDENTITY_PROOF -> "identity-proofs";
            case ADDRESS_PROOF -> "address-proofs";
            case MEDICAL_CERTIFICATE -> "medical-certificates";
            case HOMEWORK_ATTACHMENT -> "attachments";
            case SUBMISSION_ATTACHMENT -> "submissions";
            case REPORT_CARD -> "report-cards";
            case STAFF_ID -> "identity";
            case STAFF_RESUME -> "resumes";
            case STAFF_CERTIFICATE -> "certificates";
            case OTHER -> "documents";
        };
    }
}

