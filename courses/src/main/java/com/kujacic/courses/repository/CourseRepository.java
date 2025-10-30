package com.kujacic.courses.repository;

import com.kujacic.courses.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.courseLevels WHERE c.id = :id")
    Optional<Course> findCourseByIdWithLevels(@Param("id") Integer id);

    @Query("SELECT c FROM Course c WHERE c.id IN :ids")
    Page<Course> findAllByCourseIds(@Param("ids") List<Integer> courseIds, Pageable pageable);

    Page<Course> findAll(Pageable pageable);
}
