package com.kujacic.courses.utils.impl;


import com.kujacic.courses.dto.video.VideoMetadata;
import com.kujacic.courses.utils.VideoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Local filesystem implementation of VideoUtils.
 * Provides utility methods for video validation and metadata extraction from local storage.
 */
@Slf4j
@Component
public class LocalVideoUtils implements VideoUtils {

    @Value("${video.storage.path:./videos}")
    private String videoStoragePath;

    @Value("${video.upload.allowed-extensions}")
    private String allowedExtensions;

    @Value("${video.upload.max-file-size-mb}")
    private long maxFileSizeMb;

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename the filename
     * @return the file extension
     * @throws IllegalArgumentException if filename is invalid
     */
    @Override
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Retrieves metadata for a video file from local storage.
     *
     * @param videoKey the key/path of the video file
     * @return VideoMetadata containing file information
     * @throws RuntimeException if video not found or error occurs
     */
    @Override
    public VideoMetadata getVideoMetadata(String videoKey) {
        try {
            Path videoPath = Paths.get(videoStoragePath, videoKey);
            File videoFile = videoPath.toFile();

            if (!videoFile.exists()) {
                log.error("Video not found: {}", videoKey);
                throw new RuntimeException("Video not found: " + videoKey);
            }

            String contentType = Files.probeContentType(videoPath);
            if (contentType == null) {
                contentType = "video/mp4";
            }

            return VideoMetadata.builder()
                    .contentLength(videoFile.length())
                    .contentType(contentType)
                    .fileName(extractFileName(videoKey))
                    .build();

        } catch (IOException e) {
            log.error("Error fetching video metadata: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching video metadata: " + e.getMessage());
        }
    }

    /**
     * Checks if a video exists in local storage.
     *
     * @param videoKey the key/path of the video file
     * @return true if video exists, false otherwise
     */
    @Override
    public boolean videoExists(String videoKey) {
        try {
            Path videoPath = Paths.get(videoStoragePath, videoKey);
            File videoFile = videoPath.toFile();
            return videoFile.exists() && videoFile.isFile();
        } catch (Exception e) {
            log.error("Error checking video existence: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates the uploaded video file.
     *
     * @param file the file to validate
     * @throws IllegalArgumentException if validation fails
     */
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

    /**
     * Extracts the filename from a video key/path.
     *
     * @param videoKey the video key/path
     * @return the extracted filename
     */
    @Override
    public String extractFileName(String videoKey) {
        if (videoKey == null || videoKey.isEmpty()) {
            return "";
        }

        // Handle both forward and backward slashes
        int lastSlash = Math.max(videoKey.lastIndexOf("/"), videoKey.lastIndexOf("\\"));

        if (lastSlash >= 0 && lastSlash < videoKey.length() - 1) {
            return videoKey.substring(lastSlash + 1);
        }

        return videoKey;
    }
}
