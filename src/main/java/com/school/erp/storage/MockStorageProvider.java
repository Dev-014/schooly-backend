package com.school.erp.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MockStorageProvider implements ObjectStorageProvider {

    private final Map<String, byte[]> objectStore = new ConcurrentHashMap<>();
    private final Map<String, StorageObjectMetadata> metadataStore = new ConcurrentHashMap<>();

    public void putMockObject(String bucket, String key, byte[] content, String contentType) {
        String fullKey = bucket + "/" + key;
        objectStore.put(fullKey, content);
        metadataStore.put(fullKey, new StorageObjectMetadata(
                content.length,
                contentType,
                "\"mock-etag-" + System.currentTimeMillis() + "\"",
                Instant.now()
        ));
    }

    @Override
    public PresignedUploadResponse generatePresignedUploadUrl(
            String bucket,
            String key,
            String contentType,
            long maxSizeBytes,
            Duration expiration
    ) {
        String uploadUrl = String.format("https://%s.s3.mock.local/%s?upload-token=%d", bucket, key, System.currentTimeMillis());
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("x-amz-server-side-encryption", "AES256");

        // Automatically prepare a simulated mock object so complete upload calls succeed in test mode
        putMockObject(bucket, key, "%PDF-1.4 mock content".getBytes(), contentType);

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
        return String.format("https://%s.s3.mock.local/%s?disposition=%s&file=%s&expires=%d",
                bucket, key, isInline ? "inline" : "attachment", dispositionFileName, System.currentTimeMillis() + expiration.toMillis());
    }

    @Override
    public boolean doesObjectExist(String bucket, String key) {
        return metadataStore.containsKey(bucket + "/" + key);
    }

    @Override
    public StorageObjectMetadata getObjectMetadata(String bucket, String key) {
        String fullKey = bucket + "/" + key;
        StorageObjectMetadata metadata = metadataStore.get(fullKey);
        if (metadata != null) {
            return metadata;
        }
        return new StorageObjectMetadata(1024, "application/pdf", "\"default-mock-etag\"", Instant.now());
    }

    @Override
    public InputStream getObjectInputStream(String bucket, String key) {
        String fullKey = bucket + "/" + key;
        byte[] bytes = objectStore.get(fullKey);
        if (bytes == null) {
            bytes = "%PDF-1.4 simulated file content".getBytes();
        }
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void deleteObject(String bucket, String key) {
        String fullKey = bucket + "/" + key;
        objectStore.remove(fullKey);
        metadataStore.remove(fullKey);
    }

    @Override
    public void copyObject(String sourceBucket, String sourceKey, String targetBucket, String targetKey) {
        String src = sourceBucket + "/" + sourceKey;
        String dst = targetBucket + "/" + targetKey;
        if (objectStore.containsKey(src)) {
            objectStore.put(dst, objectStore.get(src));
            metadataStore.put(dst, metadataStore.get(src));
        }
    }

    @Override
    public void createFolder(String bucket, String folderKey) {
        String normalized = folderKey.endsWith("/") ? folderKey : folderKey + "/";
        putMockObject(bucket, normalized, new byte[0], "application/x-directory");
    }
}

