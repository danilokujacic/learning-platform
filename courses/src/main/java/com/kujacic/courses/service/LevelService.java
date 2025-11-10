package com.kujacic.courses.service;

import com.kujacic.courses.dto.courseLevel.CreateCourseLevelDTO;
import com.kujacic.courses.exception.CourseLevelNotFoundException;
import com.kujacic.courses.exception.CourseNotFoundException;
import com.kujacic.courses.model.Course;
import com.kujacic.courses.model.Level;
import com.kujacic.courses.repository.LevelRepository;
import com.kujacic.courses.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class LevelService {

    private final LevelRepository levelRepository;
    private final CourseRepository courseRepository;
    private final CoursePublisher coursesPublisher;


    public void createCourseLevel(Integer courseId, CreateCourseLevelDTO createCourseLevelDTO) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Course not found"));
        Level level = Level.builder().name(createCourseLevelDTO.getName()).course(course).progress(createCourseLevelDTO.getProgress()).build();
        levelRepository.save(level);
    }

    public void passCourseLevel( Integer courseId, Long levelId, String userId) {

       Level level =  levelRepository.findCourseLevelWithCourse(levelId).orElseThrow(() -> new CourseLevelNotFoundException("Could no find this course level"));


       log.info("User {} passed level {}", userId, levelId);
       this.coursesPublisher.courseLevelPublisher(courseId, levelId, userId, level.getProgress(), level.getCourse().getName());



    }
}
