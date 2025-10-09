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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final CoursesPublisher coursesPublisher;


    @Value("${RABBIT_MQ_USERNAME:NOT_SET}")
    private String rabbitMqUser;

    @Value("${RABBIT_MQ_PASSWORD:NOT_SET}")
    private String rabbitMqPassword;

    public CourseResponseDTO getCourse(Integer id) {
        log.info("COURSE ID: {}", id);
        Course course = courseRepository.findCourseByIdWithLevels(id).orElseThrow(() -> new CourseNotFoundException("Course is not found"));

        List<CourseLevelResponse> levels =  course.getCourseLevels().stream()
                .map(level -> CourseLevelResponse.builder()
                        .id(level.getId())
                        .name(level.getName())
                        .build())
                .toList();

        return CourseResponseDTO.builder()
                .id(course.getId())
                .name(course.getName())
                .level(course.getLevel())
                .courseLevels(levels)
                .build();

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
