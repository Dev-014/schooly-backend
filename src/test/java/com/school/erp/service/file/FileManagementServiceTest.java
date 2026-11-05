package com.school.erp.service.file;

import com.school.erp.dto.file.*;
import com.school.erp.entity.auth.User;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.file.FileAttachment;
import com.school.erp.entity.file.FileMetadata;
import com.school.erp.entity.superadmin.School;
import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileEntityType;
import com.school.erp.enums.file.FileStatus;
import com.school.erp.repository.auth.UserRepository;
import com.school.erp.repository.file.FileAttachmentRepository;
import com.school.erp.repository.file.FileMetadataRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.storage.MockStorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileManagementServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private FileAttachmentRepository fileAttachmentRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private MockStorageProvider storageProvider = new MockStorageProvider();

    @Mock
    private FileValidationService fileValidationService;

    @Mock
    private FileSignatureValidator fileSignatureValidator;

    @Mock
    private ClamAvScannerService clamAvScannerService;

    @Mock
    private FileSecurityService fileSecurityService;

    @Mock
    private FileAuditService fileAuditService;

    @Mock
    private AuthContextService authContextService;

    @Spy
    private FileStoragePathService fileStoragePathService = new FileStoragePathService();

    @InjectMocks
    private FileManagementService fileManagementService;

    private School school;
    private User user;
    private AuthenticatedUser authUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileManagementService, "defaultBucketName", "schooly-dev-files");
        ReflectionTestUtils.setField(fileManagementService, "downloadExpirationMinutes", 15);

        school = new School();
        school.setId(1L);

        user = new User();
        user.setId(10L);

        authUser = new AuthenticatedUser(10L, 1L, UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should successfully initiate upload and return presigned URL")
    void testInitiateUpload_Success() {
        FileUploadInitRequest request = new FileUploadInitRequest(
                "student_transcript.pdf",
                "application/pdf",
                1024L,
                FileEntityType.STUDENT,
                5L,
                FileCategory.BIRTH_CERTIFICATE
        );

        when(authContextService.requireCurrentUser()).thenReturn(authUser);
        when(authContextService.resolveSchoolId(1L)).thenReturn(1L);
        when(fileValidationService.extractExtension("student_transcript.pdf")).thenReturn("pdf");
        when(schoolRepository.getReferenceById(1L)).thenReturn(school);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        FileUploadInitResponse response = fileManagementService.initiateUpload(request, "127.0.0.1");

        assertNotNull(response);
        assertNotNull(response.fileId());
        assertTrue(response.uploadUrl().contains("upload-token"));
        assertEquals("PUT", response.httpMethod());
        verify(fileMetadataRepository).save(any(FileMetadata.class));
        verify(fileAttachmentRepository).save(any(FileAttachment.class));
    }

    @Test
    @DisplayName("Should complete upload, verify signature, and activate file")
    void testCompleteUpload_Success() {
        UUID fileId = UUID.randomUUID();
        FileMetadata file = FileMetadata.builder()
                .id(fileId)
                .school(school)
                .originalFileName("report.pdf")
                .mimeType("application/pdf")
                .fileExtension("pdf")
                .bucketName("schooly-dev-files")
                .storageKey("schools/1/STUDENT/5/BIRTH_CERTIFICATE/" + fileId + ".pdf")
                .status(FileStatus.PENDING_UPLOAD)
                .fileSizeBytes(1024L)
                .build();

        // Put mock object into mock storage
        storageProvider.putMockObject("schooly-dev-files", file.getStorageKey(), "%PDF-1.4 sample content".getBytes(), "application/pdf");

        when(authContextService.requireCurrentUser()).thenReturn(authUser);
        when(fileMetadataRepository.findByIdAndDeletedAtIsNull(fileId)).thenReturn(Optional.of(file));
        when(fileAttachmentRepository.findByFileId(fileId)).thenReturn(Optional.empty());

        FileResponseDto response = fileManagementService.completeUpload(fileId, new FileUploadCompleteRequest("sha256-hash"), "127.0.0.1");

        assertNotNull(response);
        assertEquals(FileStatus.ACTIVE, file.getStatus());
        assertEquals("sha256-hash", file.getChecksumSha256());
        verify(fileMetadataRepository).save(file);
    }

    @Test
    @DisplayName("Should generate presigned download URL for active file")
    void testGetDownloadUrl_Success() {
        UUID fileId = UUID.randomUUID();
        FileMetadata file = FileMetadata.builder()
                .id(fileId)
                .school(school)
                .originalFileName("cert.pdf")
                .mimeType("application/pdf")
                .bucketName("schooly-dev-files")
                .storageKey("schools/1/STUDENT/5/BIRTH_CERTIFICATE/" + fileId + ".pdf")
                .status(FileStatus.ACTIVE)
                .fileSizeBytes(2048L)
                .build();

        when(authContextService.requireCurrentUser()).thenReturn(authUser);
        when(fileMetadataRepository.findByIdAndDeletedAtIsNull(fileId)).thenReturn(Optional.of(file));

        FileDownloadResponse response = fileManagementService.getDownloadUrl(fileId, true, "127.0.0.1");

        assertNotNull(response);
        assertTrue(response.isInline());
        assertTrue(response.downloadUrl().contains("disposition=inline"));
    }

    @Test
    @DisplayName("Should soft delete file and unlink attachment")
    void testDeleteFile_Success() {
        UUID fileId = UUID.randomUUID();
        FileMetadata file = FileMetadata.builder()
                .id(fileId)
                .school(school)
                .status(FileStatus.ACTIVE)
                .build();

        when(authContextService.requireCurrentUser()).thenReturn(authUser);
        when(fileMetadataRepository.findByIdAndDeletedAtIsNull(fileId)).thenReturn(Optional.of(file));

        fileManagementService.deleteFile(fileId, "127.0.0.1");

        assertEquals(FileStatus.DELETED, file.getStatus());
        assertNotNull(file.getDeletedAt());
        verify(fileAttachmentRepository).deleteByFileId(fileId);
        verify(fileMetadataRepository).save(file);
    }
}

