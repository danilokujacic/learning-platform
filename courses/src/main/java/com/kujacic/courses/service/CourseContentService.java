package com.kujacic.courses.service;

import com.kujacic.courses.dto.video.VideoChunk;
import com.kujacic.courses.dto.video.VideoMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseContentService {

    private final S3Client s3Client;

    private static final long CHUNK_SIZE = 1024 * 1024; // 1MB chunks

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${video.upload.allowed-extensions}")
    private String allowedExtensions;

    @Value("${video.upload.max-file-size-mb}")
    private long maxFileSizeMb;

    public String uploadVideo(MultipartFile file) throws IOException {
        // Validate file
        validateVideo(file);

        // Generate unique file name
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
        String s3Key = "videos/" + uniqueFileName;

        try {
            // Prepare S3 request
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // Upload to S3
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Successfully uploaded video: {} to S3 bucket: {}", s3Key, bucketName);

            // Return the S3 URL
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, s3Key);

        } catch (S3Exception e) {
            log.error("Failed to upload video to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload video to S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    private void validateVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate file size
        long fileSizeInMb = file.getSize() / (1024 * 1024);
        if (fileSizeInMb > maxFileSizeMb) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size of %d MB", maxFileSizeMb));
        }

        // Validate file extension
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

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("File must be a video");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

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

    /**
     * Stream full video
     */
    public InputStream getFullVideo(String videoKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(videoKey)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return new ByteArrayInputStream(objectBytes.asByteArray());

        } catch (S3Exception e) {
            log.error("Error streaming video: {}", e.getMessage(), e);
            throw new RuntimeException("Error streaming video");
        }
    }

    /**
     * Stream video with range support (for seeking)
     */
    public VideoChunk getVideoChunk(String videoKey, long start, long end) {
        try {
            VideoMetadata metadata = getVideoMetadata(videoKey);
            long contentLength = metadata.getContentLength();

            // Adjust end if it exceeds content length
            if (end >= contentLength) {
                end = contentLength - 1;
            }

            // Validate range
            if (start > end || start < 0) {
                throw new IllegalArgumentException("Invalid byte range");
            }

            String range = String.format("bytes=%d-%d", start, end);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(videoKey)
                    .range(range)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

            return VideoChunk.builder()
                    .data(objectBytes.asByteArray())
                    .rangeStart(start)
                    .rangeEnd(end)
                    .contentLength(contentLength)
                    .contentType(metadata.getContentType())
                    .fileName(metadata.getFileName())
                    .build();

        } catch (S3Exception e) {
            log.error("Error streaming video chunk: {}", e.getMessage(), e);
            throw new RuntimeException("Error streaming video chunk");
        }
    }

    /**
     * Check if video exists
     */
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

    private String extractFileName(String videoKey) {
        return videoKey.substring(videoKey.lastIndexOf("/") + 1);
    }
}