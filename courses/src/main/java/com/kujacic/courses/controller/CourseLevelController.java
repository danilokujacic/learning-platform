package com.kujacic.courses.controller;

import com.kujacic.courses.dto.courseLevel.CreateCourseLevelDTO;
import com.kujacic.courses.service.CourseLevelsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseLevelController {
    private final CourseLevelsService courseLevelsService;

    @PostMapping("{id}/course-levels")
    public ResponseEntity<Void> createCourseLevel(@PathVariable Integer id, @Valid @RequestBody CreateCourseLevelDTO createCourseLevelDTO) {
        courseLevelsService.createCourseLevel(id, createCourseLevelDTO);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    @PostMapping("{id}/course-levels/pass/{levelId}")
    public ResponseEntity<Void> passCourseLevel(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id, @PathVariable Long levelId) {
        String userId = jwt.getClaimAsString("sub");
        courseLevelsService.passCourseLevel(id, levelId, userId);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }
}
