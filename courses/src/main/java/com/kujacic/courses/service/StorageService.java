package com.kujacic.courses.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {

    /**
     * Uploads a video file to S3 storage.
     *
     * @param contentId the unique identifier for the content
     * @param file the video file to upload
     * @return the S3 URL of the uploaded video
     * @throws IOException if an error occurs during file processing
     */
    String uploadVideo(String contentId, MultipartFile file) throws IOException;

    /**
     * Downloads a video file from S3 storage.
     *
     * @param videoId the unique identifier of the video to download
     * @return ResponseEntity containing the video resource and appropriate headers
     */
    ResponseEntity<Resource> download(String videoId);

    /**
     * Streams a video with support for range requests (partial content).
     *
     * @param videoId the unique identifier of the video to stream
     * @param rangeHeader the HTTP Range header value for partial content requests (can be null)
     * @return ResponseEntity containing the video bytes and appropriate headers for streaming
     */
    ResponseEntity<byte[]> streamVideo(String videoId, String rangeHeader);
}
