package com.school.erp.service.file;

import com.school.erp.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

@Slf4j
@Component
public class FileSignatureValidator {

    private final Tika tika = new Tika();

    private static final Set<String> DANGEROUS_MIME_TYPES = Set.of(
            "application/x-msdownload",
            "application/x-dosexec",
            "application/x-executable",
            "application/x-sh",
            "application/x-bat",
            "text/x-perl",
            "text/x-python",
            "text/javascript",
            "application/javascript"
    );

    public void validateFileSignature(InputStream inputStream, String expectedMimeType, String extension) {
        try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            bis.mark(4096);
            byte[] header = new byte[Math.min(bis.available() > 0 ? bis.available() : 4096, 4096)];
            int bytesRead = bis.read(header);
            bis.reset();

            if (bytesRead < 4) {
                throw new BadRequestException("File is empty or corrupted");
            }

            // Check executable signatures
            if (isWindowsExecutable(header) || isElfExecutable(header)) {
                log.warn("Detected executable signature in uploaded file");
                throw new BadRequestException("Executable files are strictly forbidden");
            }

            // Detect mime type using Apache Tika
            String detectedMimeType = tika.detect(header);
            log.debug("Tika detected MIME: {}, declared MIME: {}", detectedMimeType, expectedMimeType);

            if (DANGEROUS_MIME_TYPES.contains(detectedMimeType)) {
                throw new BadRequestException("Dangerous file type detected: " + detectedMimeType);
            }

            // Verify basic extension vs header matching
            if ("pdf".equalsIgnoreCase(extension) && !isPdf(header)) {
                throw new BadRequestException("File content does not match PDF signature");
            }
            if (("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) && !isJpeg(header)) {
                throw new BadRequestException("File content does not match JPEG signature");
            }
            if ("png".equalsIgnoreCase(extension) && !isPng(header)) {
                throw new BadRequestException("File content does not match PNG signature");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to inspect file signature", e);
            throw new BadRequestException("Could not verify file integrity: " + e.getMessage());
        }
    }

    private boolean isWindowsExecutable(byte[] b) {
        return b.length >= 2 && b[0] == 'M' && b[1] == 'Z';
    }

    private boolean isElfExecutable(byte[] b) {
        return b.length >= 4 && b[0] == 0x7F && b[1] == 'E' && b[2] == 'L' && b[3] == 'F';
    }

    private boolean isPdf(byte[] b) {
        return b.length >= 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F';
    }

    private boolean isJpeg(byte[] b) {
        return b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] b) {
        return b.length >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }
}

