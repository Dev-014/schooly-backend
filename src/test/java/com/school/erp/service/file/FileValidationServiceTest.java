package com.school.erp.service.file;

import com.school.erp.dto.file.FileUploadInitRequest;
import com.school.erp.enums.file.FileCategory;
import com.school.erp.enums.file.FileEntityType;
import com.school.erp.exception.BadRequestException;
import com.school.erp.repository.file.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileValidationServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @InjectMocks
    private FileValidationService validationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(validationService, "defaultSchoolQuotaBytes", 100L * 1024 * 1024); // 100 MB
    }

    @Test
    @DisplayName("Should extract valid extension successfully")
    void testExtractExtension_Valid() {
        assertEquals("pdf", validationService.extractExtension("report_card.pdf"));
        assertEquals("jpg", validationService.extractExtension("avatar.JPG"));
        assertEquals("png", validationService.extractExtension("my.profile.photo.png"));
    }

    @Test
    @DisplayName("Should reject unsupported or missing file extension")
    void testExtractExtension_Invalid() {
        assertThrows(BadRequestException.class, () -> validationService.extractExtension("no_extension"));
        assertThrows(BadRequestException.class, () -> validationService.extractExtension("malicious.exe"));
        assertThrows(BadRequestException.class, () -> validationService.extractExtension("script.sh"));
    }

    @Test
    @DisplayName("Should reject file exceeding category size limit")
    void testValidateUploadRequest_ExceedsCategoryLimit() {
        // Student photo has 2MB limit
        FileUploadInitRequest request = new FileUploadInitRequest(
                "student_photo.jpg",
                "image/jpeg",
                3L * 1024 * 1024, // 3MB > 2MB
                FileEntityType.STUDENT,
                1L,
                FileCategory.STUDENT_PHOTO
        );

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validationService.validateUploadRequest(1L, request));
        assertTrue(ex.getMessage().contains("exceeds the maximum allowed size"));
    }

    @Test
    @DisplayName("Should allow file within category size limit and school quota")
    void testValidateUploadRequest_Valid() {
        FileUploadInitRequest request = new FileUploadInitRequest(
                "birth_cert.pdf",
                "application/pdf",
                1024L * 1024L, // 1MB < 10MB
                FileEntityType.STUDENT,
                1L,
                FileCategory.BIRTH_CERTIFICATE
        );

        when(fileMetadataRepository.calculateTotalUsedBytesBySchoolId(1L)).thenReturn(10L * 1024 * 1024);

        assertDoesNotThrow(() -> validationService.validateUploadRequest(1L, request));
    }

    @Test
    @DisplayName("Should reject upload when school storage quota is exceeded")
    void testValidateUploadRequest_QuotaExceeded() {
        FileUploadInitRequest request = new FileUploadInitRequest(
                "assignment.pdf",
                "application/pdf",
                5L * 1024 * 1024,
                FileEntityType.HOMEWORK_ASSIGNMENT,
                10L,
                FileCategory.HOMEWORK_ATTACHMENT
        );

        when(fileMetadataRepository.calculateTotalUsedBytesBySchoolId(1L))
                .thenReturn(98L * 1024 * 1024); // 98MB used + 5MB > 100MB quota

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validationService.validateUploadRequest(1L, request));
        assertTrue(ex.getMessage().contains("quota exceeded"));
    }
}
