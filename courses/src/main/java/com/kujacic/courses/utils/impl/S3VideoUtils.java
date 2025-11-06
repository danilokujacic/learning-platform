package com.kujacic.courses.utils.impl;

import com.kujacic.courses.dto.video.VideoMetadata;
import com.kujacic.courses.utils.VideoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3VideoUtils implements VideoUtils {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${video.upload.allowed-extensions}")
    private String allowedExtensions;

    @Value("${video.upload.max-file-size-mb}")
    private long maxFileSizeMb;

    @Override
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    @Override
    public VideoMetadata getVideoMetadata(String videoKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(videoKey)
                    .build();

            HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

            return VideoMetadata.builder()
                    .contentLength(headObjectResponse.contentLength())
                    .contentType(headObjectResponse.contentType())
                    .fileName(extractFileName(videoKey))
                    .build();

        } catch (NoSuchKeyException e) {
            log.error("Video not found: {}", videoKey);
            throw new RuntimeException("Video not found: " + videoKey);
        } catch (S3Exception e) {
            log.error("Error fetching video metadata: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching video metadata");
        }
    }

    @Override
    public boolean videoExists(String videoKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(videoKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Error checking video existence: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void validateVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        long fileSizeInMb = file.getSize() / (1024 * 1024);
        if (fileSizeInMb > maxFileSizeMb) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size of %d MB", maxFileSizeMb));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        String fileExtension = getFileExtension(filename).toLowerCase();
        List<String> allowedExtensionsList = Arrays.asList(allowedExtensions.split(","));

        if (!allowedExtensionsList.contains(fileExtension)) {
            throw new IllegalArgumentException(
                    String.format("Invalid file type. Allowed types: %s", allowedExtensions));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("File must be a video");
        }
    }

    @Override
    public String extractFileName(String videoKey) {
        return videoKey.substring(videoKey.lastIndexOf("/") + 1);
    }
}
