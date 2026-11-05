package com.school.erp.storage;

import java.io.InputStream;
import java.time.Duration;

public interface ObjectStorageProvider {

    PresignedUploadResponse generatePresignedUploadUrl(
            String bucket,
            String key,
            String contentType,
            long maxSizeBytes,
            Duration expiration
    );

    String generatePresignedDownloadUrl(
            String bucket,
            String key,
            String dispositionFileName,
            boolean isInline,
            Duration expiration
    );

    boolean doesObjectExist(String bucket, String key);

    StorageObjectMetadata getObjectMetadata(String bucket, String key);

    InputStream getObjectInputStream(String bucket, String key);

    void deleteObject(String bucket, String key);

    void copyObject(String sourceBucket, String sourceKey, String targetBucket, String targetKey);

    void createFolder(String bucket, String folderKey);
}

