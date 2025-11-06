package com.kujacic.courses.service;

import com.kujacic.courses.dto.video.VideoChunk;
import com.kujacic.courses.dto.video.VideoMetadata;
import com.kujacic.courses.utils.VideoUtils;
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

    private final VideoUtils videoUtils;
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
        videoUtils.validateVideo(file);

        // Generate unique file name
        String originalFilename = file.getOriginalFilename();
        String fileExtension = videoUtils.getFileExtension(originalFilename);
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

    public ResponseEntity<byte[]> streamVideo(String videoId, String rangeHeader) {
        try {
            String videoKey = "videos/" + videoId + ".mp4";
            log.info("Video id {}", videoId);
            if (!videoUtils.videoExists(videoKey)) {
                return ResponseEntity.notFound().build();
            }

            VideoMetadata metadata = videoUtils.getVideoMetadata(videoKey);
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

            if (!videoUtils.videoExists(videoKey)) {
                return ResponseEntity.notFound().build();
            }

            VideoMetadata metadata = videoUtils.getVideoMetadata(videoKey);
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

    private InputStream getFullVideo(String videoKey) {
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

    private ResponseEntity<byte[]> streamFullVideo(String videoKey, VideoMetadata metadata) {
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

    private ResponseEntity<byte[]> streamVideoWithRange(
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

    private VideoChunk getVideoChunk(String videoKey, long start, long end) {
        try {
            VideoMetadata metadata = videoUtils.getVideoMetadata(videoKey);
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

}