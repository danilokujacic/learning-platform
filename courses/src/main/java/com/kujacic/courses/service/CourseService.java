package com.kujacic.courses.service;

import com.kujacic.courses.dto.course.CourseRequestDTO;
import com.kujacic.courses.dto.course.CourseResponseDTO;
import com.kujacic.courses.dto.courseLevel.CourseLevelResponse;
import com.kujacic.courses.exception.CourseNotFoundException;
import com.kujacic.courses.model.Course;
import com.kujacic.courses.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;

    private CourseResponseDTO toCourseResponse(Course course, List<CourseLevelResponse> levels) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .level(course.getLevel())
                .courseLevels(levels)
                .build();
    }
    private CourseResponseDTO toCourseResponse(Course course) {
        return CourseResponseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .level(course.getLevel())
                .build();
    }


    public CourseResponseDTO getCourse(Integer id) {
        log.info("COURSE ID: {}", id);
        Course course = courseRepository.findCourseByIdWithLevels(id).orElseThrow(() -> new CourseNotFoundException("Course is not found"));

        List<CourseLevelResponse> levels =  course.getCourseLevels().stream()
                .map(level -> CourseLevelResponse.builder()
                        .id(level.getId())
                        .name(level.getName())
                        .build())
                .toList();

        return toCourseResponse(course, levels);

    }

    public CourseResponseDTO createCourse(CourseRequestDTO courseRequest) {
        Course course = new Course();
        BeanUtils.copyProperties(courseRequest, course);

        Course savedCourse = this.courseRepository.save(course);

        return  toCourseResponse(savedCourse);
    }

    public Page<CourseResponseDTO> findCourses(int page, int size, List<Integer> courseIds) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Course> courses;
        if (courseIds == null || courseIds.isEmpty()) {
            courses = courseRepository.findAll(pageable);
        } else {
            courses = courseRepository.findAllByCourseIds(courseIds, pageable);
        }

        return courses.map(this::toCourseResponse);
    }
}
