package com.kujacic.courses.controller;

import com.kujacic.courses.dto.content.ContentResponseDTO;
import com.kujacic.courses.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/courses/{courseId}/levels/{courseLevelId}/contents")
@RequiredArgsConstructor
@Slf4j
public class CourseContentController {

    private final ContentService courseContentService;


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