package com.school.erp.storage;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AmazonS3StorageProvider implements ObjectStorageProvider {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public AmazonS3StorageProvider(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public PresignedUploadResponse generatePresignedUploadUrl(
            String bucket,
            String key,
            String contentType,
            long maxSizeBytes,
            Duration expiration
    ) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        Map<String, String> requiredHeaders = new HashMap<>();
        requiredHeaders.put("Content-Type", contentType);
        requiredHeaders.put("x-amz-server-side-encryption", "AES256");

        Instant expiresAt = Instant.now().plus(expiration);
        return new PresignedUploadResponse(
                presignedRequest.url().toString(),
                "PUT",
                requiredHeaders,
                expiresAt
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
        String dispositionType = isInline ? "inline" : "attachment";
        String contentDisposition = String.format("%s; filename=\"%s\"", dispositionType, dispositionFileName);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentDisposition(contentDisposition)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    public boolean doesObjectExist(String bucket, String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            log.error("Failed to check object existence in S3: bucket={}, key={}", bucket, key, e);
            throw e;
        }
    }

    @Override
    public StorageObjectMetadata getObjectMetadata(String bucket, String key) {
        HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
        return new StorageObjectMetadata(
                response.contentLength(),
                response.contentType(),
                response.eTag(),
                response.lastModified()
        );
    }

    @Override
    public InputStream getObjectInputStream(String bucket, String key) {
        ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(
                GetObjectRequest.builder().bucket(bucket).key(key).build()
        );
        return responseInputStream;
    }

    @Override
    public void deleteObject(String bucket, String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception e) {
            log.warn("Failed to delete object from S3: bucket={}, key={}", bucket, key, e);
        }
    }

    @Override
    public void copyObject(String sourceBucket, String sourceKey, String targetBucket, String targetKey) {
        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceKey)
                .destinationBucket(targetBucket)
                .destinationKey(targetKey)
                .build());
    }

    @Override
    public void createFolder(String bucket, String folderKey) {
        String normalizedKey = folderKey.endsWith("/") ? folderKey : folderKey + "/";
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(normalizedKey)
                            .contentType("application/x-directory")
                            .serverSideEncryption(ServerSideEncryption.AES256)
                            .build(),
                    RequestBody.empty()
            );
            log.info("Created S3 folder directory: bucket={}, folder={}", bucket, normalizedKey);
        } catch (Exception e) {
            log.warn("Failed to create S3 folder directory: bucket={}, folder={}", bucket, normalizedKey, e);
        }
    }
}

