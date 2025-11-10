package com.kujacic.courses.utils;

import com.kujacic.courses.dto.video.VideoMetadata;
import org.springframework.web.multipart.MultipartFile;

public interface VideoUtils {
    String getFileExtension(String filename);
    VideoMetadata getVideoMetadata(String videoKey);
    boolean videoExists(String videoKey);
    void validateVideo(MultipartFile file);
    String extractFileName(String videoKey);
}
