package com.school.erp.service.file;

import com.school.erp.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class FileSignatureValidatorTest {

    private FileSignatureValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileSignatureValidator();
    }

    @Test
    @DisplayName("Should accept valid PDF header")
    void testValidatePdf_Success() {
        byte[] pdfBytes = "%PDF-1.4 simulated file content".getBytes();
        assertDoesNotThrow(() -> validator.validateFileSignature(new ByteArrayInputStream(pdfBytes), "application/pdf", "pdf"));
    }

    @Test
    @DisplayName("Should accept valid JPEG header")
    void testValidateJpeg_Success() {
        byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};
        assertDoesNotThrow(() -> validator.validateFileSignature(new ByteArrayInputStream(jpegBytes), "image/jpeg", "jpg"));
    }

    @Test
    @DisplayName("Should accept valid PNG header")
    void testValidatePng_Success() {
        byte[] pngBytes = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        assertDoesNotThrow(() -> validator.validateFileSignature(new ByteArrayInputStream(pngBytes), "image/png", "png"));
    }

    @Test
    @DisplayName("Should reject Windows executable masked as PDF")
    void testRejectWindowsExecutable_MaskedAsPdf() {
        byte[] mzBytes = new byte[]{'M', 'Z', 0x00, 0x01, 0x02, 0x03};
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateFileSignature(new ByteArrayInputStream(mzBytes), "application/pdf", "pdf"));
        assertTrue(ex.getMessage().contains("Executable files are strictly forbidden"));
    }

    @Test
    @DisplayName("Should reject ELF executable masked as JPG")
    void testRejectElfExecutable_MaskedAsJpg() {
        byte[] elfBytes = new byte[]{0x7F, 'E', 'L', 'F', 0x01, 0x01};
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateFileSignature(new ByteArrayInputStream(elfBytes), "image/jpeg", "jpg"));
        assertTrue(ex.getMessage().contains("Executable files are strictly forbidden"));
    }

    @Test
    @DisplayName("Should reject empty or corrupted bytes")
    void testRejectEmptyFile() {
        byte[] emptyBytes = new byte[2];
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> validator.validateFileSignature(new ByteArrayInputStream(emptyBytes), "application/pdf", "pdf"));
        assertTrue(ex.getMessage().contains("empty or corrupted"));
    }
}

