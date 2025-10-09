package com.kujacic.courses.controller;

import com.kujacic.courses.dto.course.CourseRequestDTO;
import com.kujacic.courses.dto.course.CourseResponseDTO;
import com.kujacic.courses.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
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
}
