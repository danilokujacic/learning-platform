package com.kujacic.courses.repository;

import com.kujacic.courses.model.CourseLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseLevelsRepository extends JpaRepository<CourseLevel, Long> {

    @Query("SELECT cl, c FROM CourseLevel cl JOIN FETCH cl.course c WHERE cl.id = :id")
    Optional<CourseLevel> findCourseLevelWithCourse(@Param("id") Long id);
}
