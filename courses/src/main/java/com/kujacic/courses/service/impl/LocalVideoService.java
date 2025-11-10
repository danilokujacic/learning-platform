package com.kujacic.courses.service.impl;

import com.kujacic.courses.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class LocalVideoService implements StorageService {

    @Value("${video.storage.path:./videos}")
    private String videoStoragePath;

    @Value("${video.upload.max-file-size-mb}")
    private long maxFileSizeMb;

    @Value("${video.upload.allowed-extensions}")
    private String allowedExtensions;

    /**
     * Uploads a video file to the local filesystem.
     *
     * @param contentId the unique identifier for the content
     * @param file the video file to upload
     * @return the local file path of the uploaded video
     * @throws IOException if an error occurs during file processing
     */
    @Override
    public String uploadVideo(String contentId, MultipartFile file) throws IOException {
        // Validate file
        validateVideo(file);

        // Create storage directory if it doesn't exist
        Path storagePath = Paths.get(videoStoragePath);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            log.info("Created video storage directory: {}", videoStoragePath);
        }

        // Generate unique file name
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFileName = contentId + "." + fileExtension;
        Path destinationPath = storagePath.resolve(uniqueFileName);

        try {
            // Copy file to destination
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Successfully uploaded video: {} to local storage: {}", uniqueFileName, destinationPath);

            // Return the file path
            return destinationPath.toString();

        } catch (IOException e) {
            log.error("Failed to upload video to local storage: {}", e.getMessage(), e);
            throw new IOException("Failed to upload video to local storage: " + e.getMessage());
        }
    }

    /**
     * Downloads a video file from local filesystem.
     *
     * @param videoId the unique identifier of the video to download
     * @return ResponseEntity containing the video resource and appropriate headers
     */
    @Override
    public ResponseEntity<Resource> download(String videoId) {
        try {
            Path videoPath = Paths.get(videoStoragePath, videoId + ".mp4");
            File videoFile = videoPath.toFile();

            if (!videoFile.exists()) {
                log.warn("Video not found: {}", videoPath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(videoFile);
            String contentType = Files.probeContentType(videoPath);
            if (contentType == null) {
                contentType = "video/mp4";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(videoFile.length())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + videoFile.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Streams the full video without range support.
     * Returns the complete video file in a single response.
     *
     * @param videoId the unique identifier of the video to stream
     * @param rangeHeader the HTTP Range header value (ignored in this implementation)
     * @return ResponseEntity containing the complete video bytes
     */
    @Override
    public ResponseEntity<byte[]> streamVideo(String videoId, String rangeHeader) {
        try {
            Path videoPath = Paths.get(videoStoragePath, videoId + ".mp4");
            File videoFile = videoPath.toFile();

            if (!videoFile.exists()) {
                log.warn("Video not found: {}", videoPath);
                return ResponseEntity.notFound().build();
            }

            // Read the entire file into memory
            byte[] videoBytes = Files.readAllBytes(videoPath);

            String contentType = Files.probeContentType(videoPath);
            if (contentType == null) {
                contentType = "video/mp4";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(videoBytes.length);
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

            log.info("Streaming full video: {} (size: {} bytes)", videoId, videoBytes.length);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(videoBytes);

        } catch (Exception e) {
            log.error("Error streaming video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Validates the uploaded video file.
     *
     * @param file the file to validate
     * @throws IllegalArgumentException if validation fails
     */
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

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename the filename
     * @return the file extension
     * @throws IllegalArgumentException if filename is invalid
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
