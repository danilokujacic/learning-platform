package com.kujacic.courses.dto.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoChunk {
    private byte[] data;
    private long rangeStart;
    private long rangeEnd;
    private long contentLength;
    private String contentType;
    private String fileName;
}