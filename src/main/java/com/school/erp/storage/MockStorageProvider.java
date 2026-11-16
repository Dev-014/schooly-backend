package com.school.erp.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MockStorageProvider implements ObjectStorageProvider {

    private final Map<String, byte[]> objectStore = new ConcurrentHashMap<>();
    private final Map<String, StorageObjectMetadata> metadataStore = new ConcurrentHashMap<>();
    private final Path rootUploadPath = Paths.get("uploads").toAbsolutePath().normalize();

    public MockStorageProvider() {
        try {
            Files.createDirectories(rootUploadPath);
            log.info("MockStorageProvider initialized with root storage at: {}", rootUploadPath);
        } catch (Exception e) {
            log.warn("Could not create local upload root directory {}: {}", rootUploadPath, e.getMessage());
        }
    }

    @Override
    public void putObject(String bucket, String key, byte[] content, String contentType) {
        putMockObject(bucket, key, content, contentType);
    }

    public void putMockObject(String bucket, String key, byte[] content, String contentType) {
        String fullKey = bucket + "/" + key;
        String resolvedContentType = (contentType != null && !contentType.isBlank()) ? contentType : detectMimeType(key);
        objectStore.put(fullKey, content);
        metadataStore.put(fullKey, new StorageObjectMetadata(
                content.length,
                resolvedContentType,
                "\"mock-etag-" + System.currentTimeMillis() + "\"",
                Instant.now()
        ));

        // Persist to local disk so files survive application restarts
        try {
            Path targetPath = resolveDiskPath(bucket, key);
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Persisted local file to disk [{} bytes]: {}", content.length, targetPath);
        } catch (Exception e) {
            log.warn("Failed to persist local file to disk for {}: {}", fullKey, e.getMessage());
        }
    }

    @Override
    public PresignedUploadResponse generatePresignedUploadUrl(
            String bucket,
            String key,
            String contentType,
            long maxSizeBytes,
            Duration expiration
    ) {
        String encodedBucket = URLEncoder.encode(bucket, StandardCharsets.UTF_8);
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        String uploadUrl = String.format("/api/v1/files/mock-s3-upload?bucket=%s&key=%s", encodedBucket, encodedKey);

        Map<String, String> headers = new HashMap<>();
        String resolvedContentType = (contentType != null && !contentType.isBlank()) ? contentType : detectMimeType(key);
        headers.put("Content-Type", resolvedContentType);
        headers.put("x-amz-server-side-encryption", "AES256");

        // Record initial metadata tracking
        String fullKey = bucket + "/" + key;
        metadataStore.putIfAbsent(fullKey, new StorageObjectMetadata(
                0,
                resolvedContentType,
                "\"mock-init-etag\"",
                Instant.now()
        ));

        return new PresignedUploadResponse(
                uploadUrl,
                "PUT",
                headers,
                Instant.now().plus(expiration)
        );
    }

    @Override
    public String generatePresignedDownloadUrl(
            String bucket,
            String key,
            String dispositionFileName,
            boolean isInline,
            Duration expiration
    ) {
        String encodedBucket = URLEncoder.encode(bucket, StandardCharsets.UTF_8);
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        String encodedFile = URLEncoder.encode(dispositionFileName != null ? dispositionFileName : "document", StandardCharsets.UTF_8);
        String disposition = isInline ? "inline" : "attachment";

        return String.format("/api/v1/files/mock-s3-download?bucket=%s&key=%s&disposition=%s&file=%s",
                encodedBucket, encodedKey, disposition, encodedFile);
    }

    @Override
    public boolean doesObjectExist(String bucket, String key) {
        String fullKey = bucket + "/" + key;
        if (metadataStore.containsKey(fullKey) || objectStore.containsKey(fullKey)) {
            return true;
        }
        try {
            Path targetPath = resolveDiskPath(bucket, key);
            return Files.exists(targetPath) && Files.isRegularFile(targetPath);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public StorageObjectMetadata getObjectMetadata(String bucket, String key) {
        String fullKey = bucket + "/" + key;
        StorageObjectMetadata metadata = metadataStore.get(fullKey);
        if (metadata != null && metadata.sizeBytes() > 0) {
            return metadata;
        }
        try {
            Path targetPath = resolveDiskPath(bucket, key);
            if (Files.exists(targetPath) && Files.isRegularFile(targetPath)) {
                long size = Files.size(targetPath);
                String contentType = Files.probeContentType(targetPath);
                if (contentType == null || contentType.isBlank()) {
                    contentType = detectMimeType(key);
                }
                return new StorageObjectMetadata(size, contentType, "\"disk-etag-" + targetPath.hashCode() + "\"", Files.getLastModifiedTime(targetPath).toInstant());
            }
        } catch (Exception e) {
            log.warn("Failed to read disk metadata for {}: {}", fullKey, e.getMessage());
        }

        if (metadata != null) {
            return metadata;
        }
        return new StorageObjectMetadata(1024, detectMimeType(key), "\"default-mock-etag\"", Instant.now());
    }

    @Override
    public InputStream getObjectInputStream(String bucket, String key) {
        String fullKey = bucket + "/" + key;
        byte[] bytes = objectStore.get(fullKey);
        if (bytes != null && bytes.length > 0) {
            return new ByteArrayInputStream(bytes);
        }
        try {
            Path targetPath = resolveDiskPath(bucket, key);
            if (Files.exists(targetPath) && Files.isRegularFile(targetPath)) {
                return Files.newInputStream(targetPath);
            }
        } catch (Exception e) {
            log.warn("Could not read disk file for {}: {}", fullKey, e.getMessage());
        }
        return new ByteArrayInputStream(bytes != null ? bytes : new byte[0]);
    }

    @Override
    public void deleteObject(String bucket, String key) {
        String fullKey = bucket + "/" + key;
        objectStore.remove(fullKey);
        metadataStore.remove(fullKey);
        try {
            Path targetPath = resolveDiskPath(bucket, key);
            Files.deleteIfExists(targetPath);
        } catch (Exception e) {
            log.warn("Failed to delete disk file for {}: {}", fullKey, e.getMessage());
        }
    }

    @Override
    public void copyObject(String sourceBucket, String sourceKey, String targetBucket, String targetKey) {
        byte[] bytes = null;
        try (InputStream is = getObjectInputStream(sourceBucket, sourceKey)) {
            bytes = is.readAllBytes();
        } catch (Exception ignored) {
        }
        StorageObjectMetadata meta = getObjectMetadata(sourceBucket, sourceKey);
        if (bytes != null && bytes.length > 0) {
            putMockObject(targetBucket, targetKey, bytes, meta.contentType());
        }
    }

    @Override
    public void createFolder(String bucket, String folderKey) {
        String normalized = folderKey.endsWith("/") ? folderKey : folderKey + "/";
        try {
            Path dirPath = resolveDiskPath(bucket, normalized);
            Files.createDirectories(dirPath);
        } catch (Exception ignored) {
        }
    }

    private Path resolveDiskPath(String bucket, String key) {
        // Sanitize path against directory traversal
        Path sanitized = rootUploadPath.resolve(bucket).resolve(key.replace('\\', '/')).normalize();
        if (!sanitized.startsWith(rootUploadPath)) {
            throw new SecurityException("Potential directory traversal in storage key: " + key);
        }
        return sanitized;
    }

    private String detectMimeType(String key) {
        if (key == null) return "application/octet-stream";
        String lower = key.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".csv")) return "text/csv";
        if (lower.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }
}
