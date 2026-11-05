package com.school.erp.service.file;

import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileEntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileStoragePathServiceTest {

    private FileStoragePathService pathService;

    @BeforeEach
    void setUp() {
        pathService = new FileStoragePathService();
    }

    @Test
    @DisplayName("Should generate strict folder paths for Student KYC use-case")
    void testResolveFolderPath_StudentKYC() {
        String birthCertPath = pathService.resolveFolderPath(1L, FileEntityType.STUDENT, 17L, FileCategory.BIRTH_CERTIFICATE);
        assertEquals("schools/1/students/17/birth-certificates/", birthCertPath);

        String idProofPath = pathService.resolveFolderPath(1L, FileEntityType.STUDENT, 17L, FileCategory.IDENTITY_PROOF);
        assertEquals("schools/1/students/17/identity-proofs/", idProofPath);

        String photoPath = pathService.resolveFolderPath(1L, FileEntityType.STUDENT, 17L, FileCategory.STUDENT_PHOTO);
        assertEquals("schools/1/students/17/photos/", photoPath);
    }

    @Test
    @DisplayName("Should generate strict folder paths for Homework assignments and submissions")
    void testResolveFolderPath_Homework() {
        String assignmentPath = pathService.resolveFolderPath(2L, FileEntityType.HOMEWORK_ASSIGNMENT, 42L, FileCategory.HOMEWORK_ATTACHMENT);
        assertEquals("schools/2/homework/assignments/42/", assignmentPath);

        String submissionPath = pathService.resolveFolderPath(2L, FileEntityType.HOMEWORK_SUBMISSION, 105L, FileCategory.SUBMISSION_ATTACHMENT);
        assertEquals("schools/2/homework/submissions/105/", submissionPath);
    }

    @Test
    @DisplayName("Should generate strict folder paths for Exam Report Cards")
    void testResolveFolderPath_ExamReportCard() {
        String reportCardPath = pathService.resolveFolderPath(1L, FileEntityType.EXAM_REPORT_CARD, 88L, FileCategory.REPORT_CARD);
        assertEquals("schools/1/examinations/report-cards/88/", reportCardPath);
    }

    @Test
    @DisplayName("Should generate full object key inside strict folder")
    void testGenerateObjectKey() {
        UUID fileId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        String key = pathService.generateObjectKey(1L, FileEntityType.STUDENT, 17L, FileCategory.BIRTH_CERTIFICATE, fileId, "PDF");
        assertEquals("schools/1/students/17/birth-certificates/11111111-2222-3333-4444-555555555555.pdf", key);
    }

    @Test
    @DisplayName("Should return standard school folders for bulk provisioning")
    void testGetStandardSchoolFolders() {
        List<String> folders = pathService.getStandardSchoolFolders(5L);
        assertNotNull(folders);
        assertTrue(folders.contains("schools/5/students/"));
        assertTrue(folders.contains("schools/5/examinations/report-cards/"));
        assertTrue(folders.contains("schools/5/homework/assignments/"));
        assertTrue(folders.contains("schools/5/homework/submissions/"));
        assertTrue(folders.contains("schools/5/staff/"));
        assertTrue(folders.contains("schools/5/finance/invoices/"));
        assertTrue(folders.contains("schools/5/finance/receipts/"));
        assertTrue(folders.contains("schools/5/school/branding/"));
        assertTrue(folders.contains("schools/5/general/"));
    }
}

