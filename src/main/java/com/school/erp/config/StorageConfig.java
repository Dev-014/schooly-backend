package com.school.erp.config;

import com.school.erp.storage.AmazonS3StorageProvider;
import com.school.erp.storage.MockStorageProvider;
import com.school.erp.storage.ObjectStorageProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
public class StorageConfig {

    @Value("${app.storage.provider:${APP_STORAGE_PROVIDER:s3}}")
    private String storageProvider;

    @Value("${app.aws.region:${AWS_REGION:ap-south-1}}")
    private String awsRegion;

    @Value("${app.aws.access-key-id:${AWS_ACCESS_KEY_ID:}}")
    private String accessKeyId;

    @Value("${app.aws.secret-access-key:${AWS_SECRET_ACCESS_KEY:}}")
    private String secretAccessKey;

    @Bean
    @Primary
    public ObjectStorageProvider objectStorageProvider() {
        if ("s3".equalsIgnoreCase(storageProvider)) {
            log.info("Initializing AmazonS3StorageProvider for region {}", awsRegion);
            try {
                Region region = Region.of(awsRegion);

                AwsCredentialsProvider credentialsProvider;
                if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)
                        && !accessKeyId.contains("PASTE_") && !secretAccessKey.contains("PASTE_")) {
                    log.info("Loaded explicit AWS S3 static credentials from configuration");
                    credentialsProvider = StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId.trim(), secretAccessKey.trim())
                    );
                } else {
                    log.info("Using AWS DefaultCredentialsProvider chain (IAM role, ECS Task role, or environment)");
                    credentialsProvider = DefaultCredentialsProvider.create();
                }

                S3Client s3Client = S3Client.builder()
                        .region(region)
                        .credentialsProvider(credentialsProvider)
                        .build();

                S3Presigner s3Presigner = S3Presigner.builder()
                        .region(region)
                        .credentialsProvider(credentialsProvider)
                        .build();

                return new AmazonS3StorageProvider(s3Client, s3Presigner);
            } catch (Exception e) {
                log.warn("Failed to initialize AWS S3 client ({}), falling back to MockStorageProvider", e.getMessage());
                return new MockStorageProvider();
            }
        }

        log.info("Initializing MockStorageProvider for local/test execution");
        return new MockStorageProvider();
    }
}
