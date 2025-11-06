package com.kujacic.courses.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AWSConfig {

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider())
                .build();
    }

    private AwsCredentialsProvider awsCredentialsProvider() {
        // If credentials are provided explicitly, use them
        if (accessKeyId != null && !accessKeyId.isEmpty()
                && secretAccessKey != null && !secretAccessKey.isEmpty()) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            return StaticCredentialsProvider.create(credentials);
        }
        return DefaultCredentialsProvider.create();
    }

}
