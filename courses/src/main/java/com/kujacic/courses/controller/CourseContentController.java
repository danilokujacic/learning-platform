package com.kujacic.courses.controller;

import com.kujacic.courses.dto.video.VideoChunk;
import com.kujacic.courses.dto.video.VideoMetadata;
import com.kujacic.courses.dto.video.VideoUploadResult;
import com.kujacic.courses.service.CourseContentService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/courses/{courseId}/course-contents")
@RequiredArgsConstructor
@Slf4j
public class CourseContentController {

    private final CourseContentService courseContentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCourseVideo(
            @PathVariable Long courseId,
            @RequestParam("video") MultipartFile file) {
        try {
            log.info("Uploading video for course ID: {}. File: {}, Size: {} bytes",
                    courseId, file.getOriginalFilename(), file.getSize());

            String videoUrl = courseContentService.uploadVideo(file);

            CourseVideoUploadResponse response = new CourseVideoUploadResponse(
                    "Video uploaded successfully",
                    courseId,
                    videoUrl,
                    file.getOriginalFilename(),
                    file.getSize()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Validation error for course {}: {}", courseId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (IOException e) {
            log.error("IO error during upload for course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to read file"));
        } catch (Exception e) {
            log.error("Unexpected error during upload for course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to upload video"));
        }
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<byte[]> streamCourseVideo(
            @PathVariable Long courseId,
            @PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        try {
            String videoKey = "videos/" + videoId;

            if (!courseContentService.videoExists(videoKey)) {
                log.warn("Video not found for course {}: {}", courseId, videoId);
                return ResponseEntity.notFound().build();
            }

            VideoMetadata metadata = courseContentService.getVideoMetadata(videoKey);
            long fileSize = metadata.getContentLength();

            if (rangeHeader == null) {
                log.info("Streaming full video for course {}: {}", courseId, videoId);
                return streamFullVideo(videoKey, metadata);
            }

            log.info("Streaming video with range: {} for course {}, video: {}",
                    rangeHeader, courseId, videoId);
            return streamVideoWithRange(videoKey, rangeHeader, fileSize, metadata);

        } catch (Exception e) {
            log.error("Error streaming video for course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/metadata/{videoId}")
    public ResponseEntity<VideoMetadata> getCourseVideoMetadata(
            @PathVariable Long courseId,
            @PathVariable String videoId) {
        try {
            String videoKey = "videos/" + videoId;

            if (!courseContentService.videoExists(videoKey)) {
                log.warn("Video not found for course {}: {}", courseId, videoId);
                return ResponseEntity.notFound().build();
            }

            VideoMetadata metadata = courseContentService.getVideoMetadata(videoKey);
            return ResponseEntity.ok(metadata);

        } catch (Exception e) {
            log.error("Error fetching video metadata for course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/{videoId}")
    public ResponseEntity<Resource> downloadCourseVideo(
            @PathVariable Long courseId,
            @PathVariable String videoId) {
        try {
            String videoKey = "videos/" + videoId;

            if (!courseContentService.videoExists(videoKey)) {
                log.warn("Video not found for course {}: {}", courseId, videoId);
                return ResponseEntity.notFound().build();
            }

            VideoMetadata metadata = courseContentService.getVideoMetadata(videoKey);
            InputStreamResource resource = new InputStreamResource(
                    courseContentService.getFullVideo(videoKey));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .contentLength(metadata.getContentLength())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + metadata.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading video for course {}: {}", courseId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<byte[]> streamFullVideo(String videoKey, VideoMetadata metadata) {
        long chunkSize = Math.min(1024 * 1024 * 10, metadata.getContentLength());
        VideoChunk chunk = courseContentService.getVideoChunk(videoKey, 0, chunkSize - 1);

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

            VideoChunk chunk = courseContentService.getVideoChunk(videoKey, rangeStart, rangeEnd);

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

    @Data
    @AllArgsConstructor
    private static class CourseVideoUploadResponse {
        private String message;
        private Long courseId;
        private String videoUrl;
        private String fileName;
        private long fileSizeInBytes;
    }

    @Data
    @AllArgsConstructor
    private static class ErrorResponse {
        private String error;
    }
}