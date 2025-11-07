package com.kujacic.courses.unit;

import com.kujacic.courses.dto.course.CourseRequestDTO;
import com.kujacic.courses.dto.course.CourseResponseDTO;
import com.kujacic.courses.dto.courseLevel.CourseLevelResponse;
import com.kujacic.courses.exception.CourseNotFoundException;
import com.kujacic.courses.model.Course;
import com.kujacic.courses.model.CourseLevel;
import com.kujacic.courses.repository.CourseRepository;
import com.kujacic.courses.service.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CourseServiceTests {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;


    @Test
    void shouldGetProperCourseById() {
        Integer courseId = 1;
        CourseResponseDTO courseResponse = CourseResponseDTO.builder()
                .id(courseId)
                .name("Test Course")
                .build();


        Course dummyCourse = Course.builder()
                .id(courseId)
                .name("Test Course")
                .courseLevels(new ArrayList<>())
                .build();

        when(courseRepository.findCourseByIdWithLevels(courseId)).thenReturn(Optional.of(dummyCourse));

        CourseResponseDTO result = courseService.getCourse(courseId);

        assertEquals(result.getName(), courseResponse.getName());
        assertEquals(result.getId(), courseResponse.getId());
    }

    @Test
    void shouldThrowCourseNotFoundException() {
        Integer courseId = 1;

        when(courseRepository.findCourseByIdWithLevels(courseId)).thenReturn(Optional.empty());

        CourseNotFoundException exception = assertThrows(CourseNotFoundException.class, () -> {
            courseService.getCourse(courseId);
        });

        assertEquals("Course is not found", exception.getMessage());
    }

    @Test
    void shouldGetCourseWithEmptyLevels() {
        Integer courseId = 1;
        Course course = createCourse(courseId, "Empty Course");
        course.setCourseLevels(new ArrayList<>());

        when(courseRepository.findCourseByIdWithLevels(courseId)).thenReturn(Optional.of(course));

        CourseResponseDTO result = courseService.getCourse(courseId);

        assertNotNull(result);
        assertEquals(0, result.getCourseLevels().size());
        verify(courseRepository).findCourseByIdWithLevels(courseId);
    }

    @Test
    void shouldCreateCourse() {
        CourseRequestDTO request = new CourseRequestDTO();
        request.setName("New Course");

        Course savedCourse = new Course();
        savedCourse.setId(1);
        savedCourse.setName("New Course");

        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        CourseResponseDTO result = courseService.createCourse(request);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("New Course", result.getName());

        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void shouldFindAllCoursesWithoutFilter() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        List<Course> courses = List.of(
                createCourse(1, "Course 1"),
                createCourse(2, "Course 2"),
                createCourse(3, "Course 3")
        );
        Page<Course> coursePage = new PageImpl<>(courses, pageable, courses.size());

        when(courseRepository.findAll(pageable)).thenReturn(coursePage);

        Page<CourseResponseDTO> result = courseService.findCourses(page, size, null);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
        assertEquals("Course 1", result.getContent().get(0).getName());

        verify(courseRepository).findAll(pageable);
        verify(courseRepository, never()).findAllByCourseIds(any(), any());
    }

    @Test
    void shouldFindAllCoursesWithEmptyList() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        List<Course> courses = List.of(
                createCourse(1, "Course 1"),
                createCourse(2, "Course 2")
        );
        Page<Course> coursePage = new PageImpl<>(courses, pageable, courses.size());

        when(courseRepository.findAll(pageable)).thenReturn(coursePage);

        Page<CourseResponseDTO> result = courseService.findCourses(page, size, new ArrayList<>());

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        verify(courseRepository).findAll(pageable);
        verify(courseRepository, never()).findAllByCourseIds(any(), any());
    }

    @Test
    void shouldFindCoursesByIds() {
        int page = 0;
        int size = 10;
        List<Integer> courseIds = List.of(1, 3, 5);
        Pageable pageable = PageRequest.of(page, size);

        List<Course> courses = List.of(
                createCourse(1, "Course 1"),
                createCourse(3, "Course 3"),
                createCourse(5, "Course 5")
        );
        Page<Course> coursePage = new PageImpl<>(courses, pageable, courses.size());

        when(courseRepository.findAllByCourseIds(courseIds, pageable)).thenReturn(coursePage);

        Page<CourseResponseDTO> result = courseService.findCourses(page, size, courseIds);

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());

        verify(courseRepository).findAllByCourseIds(courseIds, pageable);
        verify(courseRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldFindCoursesBySingleId() {
        int page = 0;
        int size = 10;
        List<Integer> courseIds = List.of(1);
        Pageable pageable = PageRequest.of(page, size);

        List<Course> courses = List.of(createCourse(1, "Course 1"));
        Page<Course> coursePage = new PageImpl<>(courses, pageable, courses.size());

        when(courseRepository.findAllByCourseIds(courseIds, pageable)).thenReturn(coursePage);

        Page<CourseResponseDTO> result = courseService.findCourses(page, size, courseIds);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(courseRepository).findAllByCourseIds(courseIds, pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenNoCoursesFound() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        Page<Course> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(courseRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<CourseResponseDTO> result = courseService.findCourses(page, size, null);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(courseRepository).findAll(pageable);
    }

    @Test
    void shouldHandleDifferentPageSizes() {
        int page = 1;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        List<Course> courses = List.of(
                createCourse(6, "Course 6"),
                createCourse(7, "Course 7")
        );
        Page<Course> coursePage = new PageImpl<>(courses, pageable, 10);

        when(courseRepository.findAll(pageable)).thenReturn(coursePage);

        Page<CourseResponseDTO> result = courseService.findCourses(page, size, null);

        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());

        verify(courseRepository).findAll(pageable);
    }

    @Test
    void shouldMapCourseLevelsCorrectly() {
        Integer courseId = 1;
        Course course = createCourse(courseId, "Test Course");

        CourseLevel level1 = createCourseLevel(10L, "Beginner Level");
        CourseLevel level2 = createCourseLevel(20L, "Intermediate Level");
        CourseLevel level3 = createCourseLevel(30L, "Expert Level");

        course.setCourseLevels(List.of(level1, level2, level3));

        when(courseRepository.findCourseByIdWithLevels(courseId)).thenReturn(Optional.of(course));

        CourseResponseDTO result = courseService.getCourse(courseId);

        assertEquals(3, result.getCourseLevels().size());

        CourseLevelResponse resultLevel1 = result.getCourseLevels().get(0);
        assertEquals(10L, resultLevel1.getId());
        assertEquals("Beginner Level", resultLevel1.getName());

        CourseLevelResponse resultLevel2 = result.getCourseLevels().get(1);
        assertEquals(20L, resultLevel2.getId());
        assertEquals("Intermediate Level", resultLevel2.getName());

        CourseLevelResponse resultLevel3 = result.getCourseLevels().get(2);
        assertEquals(30L, resultLevel3.getId());
        assertEquals("Expert Level", resultLevel3.getName());
    }

    @Test
    void shouldVerifyRepositorySaveCalledWithCorrectData() {
        CourseRequestDTO request = new CourseRequestDTO();
        request.setName("Test Course");

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(1);
            return course;
        });

        courseService.createCourse(request);

        verify(courseRepository).save(argThat(course ->
                course.getName().equals("Test Course")
        ));
    }

    private Course createCourse(Integer id, String name) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setCourseLevels(new ArrayList<>());
        return course;
    }

    private CourseLevel createCourseLevel(Long id, String name) {
        CourseLevel level = new CourseLevel();
        level.setId(id);
        level.setName(name);
        return level;
    }
}
