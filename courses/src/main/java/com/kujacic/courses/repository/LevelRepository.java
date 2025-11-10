package com.kujacic.courses.repository;

import com.kujacic.courses.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LevelRepository extends JpaRepository<Level, Long> {

    @Query("SELECT cl, c FROM Level cl JOIN FETCH cl.course c WHERE cl.id = :id")
    Optional<Level> findCourseLevelWithCourse(@Param("id") Long id);
}
