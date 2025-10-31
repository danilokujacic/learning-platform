package com.kujacic.courses.controller;

import com.kujacic.courses.dto.content.ContentResponseDTO;
import com.kujacic.courses.dto.video.VideoChunk;
import com.kujacic.courses.dto.video.VideoMetadata;
import com.kujacic.courses.service.CourseContentsService;
import com.kujacic.courses.service.VideoService;
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
@RequestMapping("/api/courses/{courseId}/levels/{courseLevelId}/contents")
@RequiredArgsConstructor
@Slf4j
public class CourseContentController {

    private final CourseContentsService courseContentService;


    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContentResponseDTO> createCourseContent(
            @PathVariable Long courseId,
            @PathVariable Long courseLevelId,
            @RequestParam("video") MultipartFile file
    ) {
        ContentResponseDTO contentResponse = courseContentService.createContent(courseLevelId, file);

        return ResponseEntity.ok(contentResponse);
    }
}