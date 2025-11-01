package com.kujacic.users.repository;


import com.kujacic.users.model.Progress;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Integer> {

    @Query("SELECT p FROM Progress p WHERE p.courseId = :courseId AND p.userId = :userId")
    Optional<Progress> findByCourseIdAndUserId(
            @Param("courseId") Integer courseId,
            @Param("userId") String userId
    );

    @Query(value = "SELECT * FROM progress WHERE user_id = :userId", nativeQuery = true)
    List<Progress> findByUserId(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query("UPDATE Progress p SET p.progress = :progress WHERE p.courseId = :courseId AND p.userId = :userId")
    int updateProgressByCourseIdAndUserId(
            @Param("progress") Integer progress,
            @Param("courseId") Integer courseId,
            @Param("userId") String userId
    );
}
