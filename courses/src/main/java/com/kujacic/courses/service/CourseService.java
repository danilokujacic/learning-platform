package com.kujacic.courses.service;

import com.kujacic.courses.dto.course.CourseRequestDTO;
import com.kujacic.courses.dto.course.CourseResponseDTO;
import com.kujacic.courses.model.Course;
import com.kujacic.courses.repository.CourseRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public CourseResponseDTO getCourse(Integer id) {
        Course course = this.courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));

        CourseResponseDTO courseResponse = new CourseResponseDTO();

        BeanUtils.copyProperties(course, courseResponse);

        return courseResponse;

    }

    public CourseResponseDTO createCourse(CourseRequestDTO courseRequest) {
        Course course = new Course();

        BeanUtils.copyProperties(courseRequest, course);

        this.courseRepository.save(course);

        CourseResponseDTO courseResponse = new CourseResponseDTO();

        BeanUtils.copyProperties(course, courseResponse);

        return courseResponse;
    }
}
