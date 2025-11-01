package com.kujacic.courses.unit;

import com.kujacic.courses.dto.courseLevel.CreateCourseLevelDTO;
import com.kujacic.courses.enums.CourseLevels;
import com.kujacic.courses.exception.CourseLevelNotFoundException;
import com.kujacic.courses.exception.CourseNotFoundException;
import com.kujacic.courses.model.Course;
import com.kujacic.courses.model.CourseLevel;
import com.kujacic.courses.repository.CourseLevelsRepository;
import com.kujacic.courses.repository.CourseRepository;
import com.kujacic.courses.service.CourseLevelsService;
import com.kujacic.courses.service.CoursePublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseLevelServiceTests {

    @Mock
    private CourseLevelsRepository courseLevelsRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CoursePublisher coursesPublisher;

    @InjectMocks
    private CourseLevelsService courseLevelsService;

    @Test
    void shouldCreateCourseLevelSuccessfully() {
        Integer courseId = 1;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Beginner Level", 25);
        Course course = createCourse(courseId, "Spring Boot Course", CourseLevels.JUNIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseLevelsRepository.save(any(CourseLevel.class))).thenAnswer(invocation -> {
            CourseLevel level = invocation.getArgument(0);
            level.setId(1L);
            return level;
        });

        courseLevelsService.createCourseLevel(courseId, dto);

        verify(courseRepository).findById(courseId);
        verify(courseLevelsRepository).save(any(CourseLevel.class));
    }

    @Test
    void shouldThrowExceptionWhenCourseNotFoundDuringCreate() {
        Integer courseId = 999;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Test Level", 50);

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        CourseNotFoundException exception = assertThrows(CourseNotFoundException.class, () -> {
            courseLevelsService.createCourseLevel(courseId, dto);
        });

        assertEquals("Course not found", exception.getMessage());
        verify(courseRepository).findById(courseId);
        verify(courseLevelsRepository, never()).save(any());
    }

    @Test
    void shouldSaveCourseLevelWithCorrectData() {
        Integer courseId = 1;
        String levelName = "Advanced Level";
        Integer progress = 75;
        CreateCourseLevelDTO dto = createCourseLevelDTO(levelName, progress);
        Course course = createCourse(courseId, "Java Course", CourseLevels.MEDIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto);

        ArgumentCaptor<CourseLevel> levelCaptor = ArgumentCaptor.forClass(CourseLevel.class);
        verify(courseLevelsRepository).save(levelCaptor.capture());

        CourseLevel capturedLevel = levelCaptor.getValue();
        assertEquals(levelName, capturedLevel.getName());
        assertEquals(progress, capturedLevel.getProgress());
        assertEquals(course, capturedLevel.getCourse());
    }

    @Test
    void shouldCreateCourseLevelWithZeroProgress() {
        Integer courseId = 1;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Starter Level", 0);
        Course course = createCourse(courseId, "Test Course", CourseLevels.JUNIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto);

        ArgumentCaptor<CourseLevel> levelCaptor = ArgumentCaptor.forClass(CourseLevel.class);
        verify(courseLevelsRepository).save(levelCaptor.capture());

        assertEquals(0, levelCaptor.getValue().getProgress());
    }

    @Test
    void shouldCreateCourseLevelWithMaxProgress() {
        Integer courseId = 1;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Master Level", 100);
        Course course = createCourse(courseId, "Test Course", CourseLevels.SENIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto);

        ArgumentCaptor<CourseLevel> levelCaptor = ArgumentCaptor.forClass(CourseLevel.class);
        verify(courseLevelsRepository).save(levelCaptor.capture());

        assertEquals(100, levelCaptor.getValue().getProgress());
    }

    @Test
    void shouldPassCourseLevelSuccessfully() {
        Integer courseId = 1;
        Long levelId = 10L;
        String userId = "user123";
        CourseLevel courseLevel = createCourseLevelWithCourse(levelId, "Intermediate Level", 50, courseId, "Spring Boot", CourseLevels.MEDIOR);

        when(courseLevelsRepository.findCourseLevelWithCourse(levelId))
                .thenReturn(Optional.of(courseLevel));

        courseLevelsService.passCourseLevel(courseId, levelId, userId);

        verify(courseLevelsRepository).findCourseLevelWithCourse(levelId);
        verify(coursesPublisher).courseLevelPublisher(
                eq(courseId),
                eq(levelId),
                eq(userId),
                eq(50),
                eq("Spring Boot")
        );
    }

    @Test
    void shouldThrowExceptionWhenCourseLevelNotFoundDuringPass() {
        Integer courseId = 1;
        Long levelId = 999L;
        String userId = "user123";

        when(courseLevelsRepository.findCourseLevelWithCourse(levelId))
                .thenReturn(Optional.empty());

        CourseLevelNotFoundException exception = assertThrows(CourseLevelNotFoundException.class, () -> {
            courseLevelsService.passCourseLevel(courseId, levelId, userId);
        });

        assertEquals("Could no find this course level", exception.getMessage());
        verify(courseLevelsRepository).findCourseLevelWithCourse(levelId);
        verify(coursesPublisher, never()).courseLevelPublisher(any(), any(), any(), any(), any());
    }

    @Test
    void shouldPublishWithCorrectCourseName() {
        Integer courseId = 5;
        Long levelId = 15L;
        String userId = "user456";
        String courseName = "Advanced Microservices";
        CourseLevel courseLevel = createCourseLevelWithCourse(levelId, "Level 3", 75, courseId, courseName, CourseLevels.SENIOR);

        when(courseLevelsRepository.findCourseLevelWithCourse(levelId))
                .thenReturn(Optional.of(courseLevel));

        courseLevelsService.passCourseLevel(courseId, levelId, userId);

        verify(coursesPublisher).courseLevelPublisher(
                eq(courseId),
                eq(levelId),
                eq(userId),
                eq(75),
                eq(courseName)
        );
    }

    @Test
    void shouldPublishWithCorrectProgress() {
        Integer courseId = 1;
        Long levelId = 20L;
        String userId = "user789";
        Integer progress = 90;
        CourseLevel courseLevel = createCourseLevelWithCourse(levelId, "Expert Level", progress, courseId, "Test Course", CourseLevels.SENIOR);

        when(courseLevelsRepository.findCourseLevelWithCourse(levelId))
                .thenReturn(Optional.of(courseLevel));

        courseLevelsService.passCourseLevel(courseId, levelId, userId);

        verify(coursesPublisher).courseLevelPublisher(
                any(),
                any(),
                any(),
                eq(progress),
                any()
        );
    }

    @Test
    void shouldPassMultipleCourseLevels() {
        Integer courseId = 1;
        Long levelId1 = 1L;
        Long levelId2 = 2L;
        String userId = "user123";

        CourseLevel level1 = createCourseLevelWithCourse(levelId1, "Level 1", 25, courseId, "Course", CourseLevels.JUNIOR);
        CourseLevel level2 = createCourseLevelWithCourse(levelId2, "Level 2", 50, courseId, "Course", CourseLevels.MEDIOR);

        when(courseLevelsRepository.findCourseLevelWithCourse(levelId1)).thenReturn(Optional.of(level1));
        when(courseLevelsRepository.findCourseLevelWithCourse(levelId2)).thenReturn(Optional.of(level2));

        courseLevelsService.passCourseLevel(courseId, levelId1, userId);
        courseLevelsService.passCourseLevel(courseId, levelId2, userId);

        verify(coursesPublisher, times(2)).courseLevelPublisher(any(), any(), any(), any(), any());
    }

    @Test
    void shouldCreateMultipleCourseLevels() {
        Integer courseId = 1;
        Course course = createCourse(courseId, "Test Course", CourseLevels.JUNIOR);

        CreateCourseLevelDTO dto1 = createCourseLevelDTO("Level 1", 25);
        CreateCourseLevelDTO dto2 = createCourseLevelDTO("Level 2", 50);
        CreateCourseLevelDTO dto3 = createCourseLevelDTO("Level 3", 75);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto1);
        courseLevelsService.createCourseLevel(courseId, dto2);
        courseLevelsService.createCourseLevel(courseId, dto3);

        verify(courseLevelsRepository, times(3)).save(any(CourseLevel.class));
    }

    @Test
    void shouldAssociateLevelWithCorrectCourse() {
        Integer courseId = 5;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Test Level", 50);
        Course course = createCourse(courseId, "Specific Course", CourseLevels.MEDIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto);

        ArgumentCaptor<CourseLevel> levelCaptor = ArgumentCaptor.forClass(CourseLevel.class);
        verify(courseLevelsRepository).save(levelCaptor.capture());

        assertEquals(course, levelCaptor.getValue().getCourse());
        assertEquals(courseId, levelCaptor.getValue().getCourse().getId());
    }

    @Test
    void shouldHandleRepositoryExceptionDuringCreate() {
        Integer courseId = 1;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Test Level", 50);
        Course course = createCourse(courseId, "Test Course", CourseLevels.JUNIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseLevelsRepository.save(any(CourseLevel.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            courseLevelsService.createCourseLevel(courseId, dto);
        });

        verify(courseRepository).findById(courseId);
    }

    @Test
    void shouldHandlePublisherExceptionDuringPass() {
        Integer courseId = 1;
        Long levelId = 10L;
        String userId = "user123";
        CourseLevel courseLevel = createCourseLevelWithCourse(levelId, "Level", 50, courseId, "Course", CourseLevels.JUNIOR);

        when(courseLevelsRepository.findCourseLevelWithCourse(levelId))
                .thenReturn(Optional.of(courseLevel));
        doThrow(new RuntimeException("RabbitMQ error"))
                .when(coursesPublisher).courseLevelPublisher(any(), any(), any(), any(), any());

        assertThrows(RuntimeException.class, () -> {
            courseLevelsService.passCourseLevel(courseId, levelId, userId);
        });

        verify(courseLevelsRepository).findCourseLevelWithCourse(levelId);
    }

    @Test
    void shouldVerifyRepositoryCalledOnceForCreate() {
        Integer courseId = 1;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Test Level", 50);
        Course course = createCourse(courseId, "Test Course", CourseLevels.JUNIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto);

        verify(courseRepository, times(1)).findById(courseId);
        verify(courseLevelsRepository, times(1)).save(any(CourseLevel.class));
    }

    @Test
    void shouldVerifyRepositoryCalledOnceForPass() {
        Integer courseId = 1;
        Long levelId = 10L;
        String userId = "user123";
        CourseLevel courseLevel = createCourseLevelWithCourse(levelId, "Level", 50, courseId, "Course", CourseLevels.JUNIOR);

        when(courseLevelsRepository.findCourseLevelWithCourse(levelId))
                .thenReturn(Optional.of(courseLevel));

        courseLevelsService.passCourseLevel(courseId, levelId, userId);

        verify(courseLevelsRepository, times(1)).findCourseLevelWithCourse(levelId);
        verify(coursesPublisher, times(1)).courseLevelPublisher(any(), any(), any(), any(), any());
    }

    @Test
    void shouldPassLevelWithDifferentUsers() {
        Integer courseId = 1;
        Long levelId = 10L;
        CourseLevel courseLevel = createCourseLevelWithCourse(levelId, "Level", 50, courseId, "Course", CourseLevels.MEDIOR);

        when(courseLevelsRepository.findCourseLevelWithCourse(levelId))
                .thenReturn(Optional.of(courseLevel));

        courseLevelsService.passCourseLevel(courseId, levelId, "user1");
        courseLevelsService.passCourseLevel(courseId, levelId, "user2");
        courseLevelsService.passCourseLevel(courseId, levelId, "user3");

        verify(coursesPublisher).courseLevelPublisher(eq(courseId), eq(levelId), eq("user1"), any(), any());
        verify(coursesPublisher).courseLevelPublisher(eq(courseId), eq(levelId), eq("user2"), any(), any());
        verify(coursesPublisher).courseLevelPublisher(eq(courseId), eq(levelId), eq("user3"), any(), any());
    }

    @Test
    void shouldCreateLevelWithMinimumProgress() {
        Integer courseId = 1;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Minimum Level", 0);
        Course course = createCourse(courseId, "Test Course", CourseLevels.JUNIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto);

        ArgumentCaptor<CourseLevel> levelCaptor = ArgumentCaptor.forClass(CourseLevel.class);
        verify(courseLevelsRepository).save(levelCaptor.capture());

        CourseLevel saved = levelCaptor.getValue();
        assertEquals(0, saved.getProgress());
        assertEquals("Minimum Level", saved.getName());
    }

    @Test
    void shouldCreateLevelWithMaximumProgress() {
        Integer courseId = 1;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Maximum Level", 100);
        Course course = createCourse(courseId, "Test Course", CourseLevels.SENIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto);

        ArgumentCaptor<CourseLevel> levelCaptor = ArgumentCaptor.forClass(CourseLevel.class);
        verify(courseLevelsRepository).save(levelCaptor.capture());

        CourseLevel saved = levelCaptor.getValue();
        assertEquals(100, saved.getProgress());
        assertEquals("Maximum Level", saved.getName());
    }

    @Test
    void shouldPublishAllCourseLevelData() {
        Integer courseId = 10;
        Long levelId = 20L;
        String userId = "specificUser";
        Integer progress = 65;
        String courseName = "Specific Course Name";

        CourseLevel courseLevel = createCourseLevelWithCourse(levelId, "Level", progress, courseId, courseName, CourseLevels.MEDIOR);

        when(courseLevelsRepository.findCourseLevelWithCourse(levelId))
                .thenReturn(Optional.of(courseLevel));

        courseLevelsService.passCourseLevel(courseId, levelId, userId);

        verify(coursesPublisher).courseLevelPublisher(
                eq(courseId),
                eq(levelId),
                eq(userId),
                eq(progress),
                eq(courseName)
        );
    }

    @Test
    void shouldCreateCourseLevelForJuniorCourse() {
        Integer courseId = 1;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Junior Level", 30);
        Course course = createCourse(courseId, "Beginner Course", CourseLevels.JUNIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto);

        ArgumentCaptor<CourseLevel> levelCaptor = ArgumentCaptor.forClass(CourseLevel.class);
        verify(courseLevelsRepository).save(levelCaptor.capture());

        assertEquals(CourseLevels.JUNIOR, levelCaptor.getValue().getCourse().getLevel());
    }

    @Test
    void shouldCreateCourseLevelForMediorCourse() {
        Integer courseId = 2;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Medior Level", 60);
        Course course = createCourse(courseId, "Intermediate Course", CourseLevels.MEDIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto);

        ArgumentCaptor<CourseLevel> levelCaptor = ArgumentCaptor.forClass(CourseLevel.class);
        verify(courseLevelsRepository).save(levelCaptor.capture());

        assertEquals(CourseLevels.MEDIOR, levelCaptor.getValue().getCourse().getLevel());
    }

    @Test
    void shouldCreateCourseLevelForSeniorCourse() {
        Integer courseId = 3;
        CreateCourseLevelDTO dto = createCourseLevelDTO("Senior Level", 90);
        Course course = createCourse(courseId, "Advanced Course", CourseLevels.SENIOR);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        courseLevelsService.createCourseLevel(courseId, dto);

        ArgumentCaptor<CourseLevel> levelCaptor = ArgumentCaptor.forClass(CourseLevel.class);
        verify(courseLevelsRepository).save(levelCaptor.capture());

        assertEquals(CourseLevels.SENIOR, levelCaptor.getValue().getCourse().getLevel());
    }

    private CreateCourseLevelDTO createCourseLevelDTO(String name, Integer progress) {
        CreateCourseLevelDTO dto = new CreateCourseLevelDTO();
        dto.setName(name);
        dto.setProgress(progress);
        return dto;
    }

    private Course createCourse(Integer id, String name, CourseLevels level) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setLevel(level);
        return course;
    }

    private CourseLevel createCourseLevelWithCourse(Long levelId, String levelName, Integer progress, Integer courseId, String courseName, CourseLevels courseLevel) {
        Course course = createCourse(courseId, courseName, courseLevel);

        return CourseLevel.builder()
                .id(levelId)
                .name(levelName)
                .progress(progress)
                .course(course)
                .build();
    }
}