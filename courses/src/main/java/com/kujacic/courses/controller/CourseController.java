package com.kujacic.courses.controller;

import com.kujacic.courses.dto.course.CourseRequestDTO;
import com.kujacic.courses.dto.course.CourseResponseDTO;
import com.kujacic.courses.dto.course.QueryCourseDTO;
import com.kujacic.courses.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponseDTO> createCourse(@Valid @RequestBody CourseRequestDTO courseRequest) {
        CourseResponseDTO courseResponse = courseService.createCourse(courseRequest);

        return new ResponseEntity<>(courseResponse, HttpStatus.OK);
    };

    @GetMapping("{id}")
    public ResponseEntity<CourseResponseDTO> getCourse(@PathVariable Integer id){
        CourseResponseDTO courseResponse = courseService.getCourse(id);
        return new ResponseEntity<>(courseResponse, HttpStatus.OK);
    }

    @PostMapping("query")
    public Page<CourseResponseDTO> queryCourses(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestBody QueryCourseDTO queryCourseDTO ) {

        log.info("Course ids {}", queryCourseDTO.getCourseIds());
        return courseService.findCourses(page, size, queryCourseDTO.getCourseIds());
    }


}
