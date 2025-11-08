package com.kujacic.courses.repository;

import com.kujacic.courses.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {
    @Query("SELECT cc FROM CourseContent cc JOIN FETCH cc.courseLevel WHERE cc.id = :id")
    Optional<Content> findByIdWithCourse(@Param("id") UUID id);

    @Query("SELECT COUNT(cc) > 0 FROM CourseContent cc WHERE cc.courseLevel.id = :id")
    Boolean existByCourseLevelId(@Param("id") Long id);
}
