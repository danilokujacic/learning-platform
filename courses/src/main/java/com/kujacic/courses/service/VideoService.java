package com.kujacic.courses.service;

import com.kujacic.courses.dto.video.VideoChunk;
import com.kujacic.courses.dto.video.VideoMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class VideoService {

    private final S3Client s3Client;

    private static final long CHUNK_SIZE = 1024 * 1024; // 1MB chunks

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${video.upload.allowed-extensions}")
    private String allowedExtensions;

    @Value("${video.upload.max-file-size-mb}")
    private long maxFileSizeMb;

    public String uploadVideo(String contentId,  MultipartFile file) throws IOException {
        // Validate file
        validateVideo(file);

        // Generate unique file name
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFileName = contentId + "." + fileExtension;
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

    public ResponseEntity<byte[]> streamVideo(String videoId, String rangeHeader) {
        try {
            String videoKey = "videos/" + videoId + ".mp4";
            log.info("Video id {}", videoId);
            if (!videoExists(videoKey)) {
                return ResponseEntity.notFound().build();
            }

            VideoMetadata metadata = getVideoMetadata(videoKey);
            long fileSize = metadata.getContentLength();

            if (rangeHeader == null) {
                return streamFullVideo(videoKey, metadata);
            }

            return streamVideoWithRange(videoKey, rangeHeader, fileSize, metadata);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    public ResponseEntity<Resource> download(String videoId) {
        try {
            String videoKey = "videos/" + videoId + ".mp4";

            if (!videoExists(videoKey)) {
                return ResponseEntity.notFound().build();
            }

            VideoMetadata metadata = getVideoMetadata(videoKey);
            InputStreamResource resource = new InputStreamResource(getFullVideo(videoKey));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .contentLength(metadata.getContentLength())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + metadata.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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

    public ResponseEntity<byte[]> streamFullVideo(String videoKey, VideoMetadata metadata) {
        long chunkSize = Math.min(1024 * 1024 * 10, metadata.getContentLength());
        VideoChunk chunk = getVideoChunk(videoKey, 0, chunkSize - 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
        headers.setContentLength(chunk.getData().length);
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.set(HttpHeaders.CONTENT_RANGE,
                String.format("bytes %d-%d/%d", 0, chunkSize - 1, metadata.getContentLength()));

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .body(chunk.getData());
    }

    public ResponseEntity<byte[]> streamVideoWithRange(
            String videoKey, String rangeHeader, long fileSize, VideoMetadata metadata) {

        try {
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long rangeStart = Long.parseLong(ranges[0]);
            long rangeEnd;

            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                rangeEnd = Long.parseLong(ranges[1]);
            } else {
                rangeEnd = Math.min(rangeStart + (1024 * 1024 * 10) - 1, fileSize - 1);
            }

            if (rangeStart >= fileSize) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            VideoChunk chunk = getVideoChunk(videoKey, rangeStart, rangeEnd);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
            headers.setContentLength(chunk.getData().length);
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            headers.set(HttpHeaders.CONTENT_RANGE,
                    String.format("bytes %d-%d/%d",
                            chunk.getRangeStart(),
                            chunk.getRangeEnd(),
                            chunk.getContentLength()));
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            log.info("Sending video chunk: bytes {}-{}/{}",
                    chunk.getRangeStart(), chunk.getRangeEnd(), chunk.getContentLength());

            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(chunk.getData());

        } catch (NumberFormatException e) {
            log.error("Invalid range header: {}", rangeHeader);
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid byte range: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                    .build();
        }
    }

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