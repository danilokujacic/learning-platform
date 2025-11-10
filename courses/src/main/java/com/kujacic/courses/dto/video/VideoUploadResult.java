package com.kujacic.courses.dto.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoUploadResult {
    private String videoId;
    private String videoUrl;
    private String s3Key;
}