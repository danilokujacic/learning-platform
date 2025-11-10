package com.kujacic.courses.dto.video;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoMetadata {
    private Long contentLength;
    private String contentType;
    private String fileName;
}
