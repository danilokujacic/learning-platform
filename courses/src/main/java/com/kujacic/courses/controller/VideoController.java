package com.kujacic.courses.controller;

import com.kujacic.courses.dto.video.VideoPost;
import com.kujacic.courses.service.StorageService;
import com.kujacic.courses.service.impl.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {
    private final StorageService videoService;

    @PostMapping("stream")
    public ResponseEntity<byte[]> streamVideo(@Valid @RequestBody VideoPost videoPostRequest, @RequestHeader(value = "Range", required = false) String rangeHeader) {
        return videoService.streamVideo(videoPostRequest.getVideoId(), rangeHeader);
    }

    @PostMapping("download")
    public ResponseEntity<Resource> downloadVideo(@Valid @RequestBody VideoPost videoPostRequest) {
        return videoService.download(videoPostRequest.getVideoId());
    }
}
