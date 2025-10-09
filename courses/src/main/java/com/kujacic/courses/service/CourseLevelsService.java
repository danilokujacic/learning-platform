package com.kujacic.courses.service;

import com.kujacic.courses.dto.courseLevel.CreateCourseLevelDTO;
import com.kujacic.courses.exception.CourseLevelNotFoundException;
import com.kujacic.courses.exception.CourseNotFoundException;
import com.kujacic.courses.model.Course;
import com.kujacic.courses.model.CourseLevel;
import com.kujacic.courses.repository.CourseLevelsRepository;
import com.kujacic.courses.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class CourseLevelsService {

    private final CourseLevelsRepository courseLevelsRepository;
    private final CourseRepository courseRepository;
    private final CoursesPublisher coursesPublisher;


    public void createCourseLevel(Integer courseId, CreateCourseLevelDTO createCourseLevelDTO) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Course not found"));
        CourseLevel courseLevel = CourseLevel.builder().name(createCourseLevelDTO.getName()).course(course).progress(createCourseLevelDTO.getProgress()).build();
        courseLevelsRepository.save(courseLevel);
    }

    public void passCourseLevel( Integer courseId, Long levelId, String userId) {

       CourseLevel courseLevel =  courseLevelsRepository.findById(levelId).orElseThrow(() -> new CourseLevelNotFoundException("Could no find this course level"));


       log.info("User {} passed level {}", userId, levelId);
       this.coursesPublisher.courseLevelPublisher(courseId, levelId, userId, courseLevel.getProgress());



    }
}
