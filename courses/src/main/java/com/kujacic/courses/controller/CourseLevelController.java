package com.kujacic.courses.controller;

import com.kujacic.courses.dto.courseLevel.CourseLevelUpdatedResponse;
import com.kujacic.courses.dto.courseLevel.CreateCourseLevelDTO;
import com.kujacic.courses.dto.courseLevel.UpdateCourseLevelStatus;
import com.kujacic.courses.enums.CourseLevelPassedStatus;
import com.kujacic.courses.service.CourseLevelsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/{courseId}/levels")
@RequiredArgsConstructor
public class CourseLevelController {
    private final CourseLevelsService courseLevelsService;

    @PostMapping()
    public ResponseEntity<Void> createCourseLevel(@PathVariable Integer courseId, @Valid @RequestBody CreateCourseLevelDTO createCourseLevelDTO) {
        courseLevelsService.createCourseLevel(courseId, createCourseLevelDTO);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    @PatchMapping("{levelId}")
    public ResponseEntity<CourseLevelUpdatedResponse> passCourseLevel(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer courseId, @PathVariable Long levelId, @Valid @RequestBody UpdateCourseLevelStatus updateCourseLevelStatus) {
        String userId = jwt.getClaimAsString("sub");
        if(updateCourseLevelStatus.getStatus().equals(CourseLevelPassedStatus.PASSED)) {
            courseLevelsService.passCourseLevel(courseId, levelId, userId);
        }
        CourseLevelUpdatedResponse courseLevelUpdatedResponse = CourseLevelUpdatedResponse.builder().courseLevelPassedStatus(CourseLevelPassedStatus.PASSED).build();
        return new ResponseEntity<>(courseLevelUpdatedResponse, HttpStatus.OK);
    }
}
